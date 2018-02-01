package echo.actor.store

import java.sql.{Connection, DriverManager}
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.EntityManager
import javax.transaction.Transactional

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.devskiller.friendly_id.Url62
import echo.actor.directory.repository.RepositoryFactoryBuilder
import echo.actor.directory.service.{EpisodeService, FeedService, PodcastService}
import echo.actor.protocol.ActorMessages._
import echo.core.model.dto.{EpisodeDTO, FeedDTO, PodcastDTO}
import echo.core.model.feed.FeedStatus
import liquibase.database.jvm.JdbcConnection
import liquibase.database.{Database, DatabaseFactory}
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.{Contexts, LabelExpression, Liquibase}

import scala.collection.JavaConverters._
import scala.io.Source

/**
  * @author Maximilian Irro
  */
class DirectoryStore extends Actor with ActorLogging {

    private var crawler: ActorRef = _
    private var indexStore: ActorRef = _

    // I need this to run liquibase
    //val appCtx = new ClassPathXmlApplicationContext("application-context.xml")
    /* TODO these I used before
    val podcastDao: PodcastDao = appCtx.getBean(classOf[PodcastDao])
    val episodeDao: EpisodeDao = appCtx.getBean(classOf[EpisodeDao])
    val feedDao: FeedDao = appCtx.getBean(classOf[FeedDao])
    */

    /*
    def transactionManager(emf: EntityManagerFactory): PlatformTransactionManager = {
        val transactionManager = new JpaTransactionManager
        transactionManager.setEntityManagerFactory(emf)
        transactionManager
    }
    */

    runLiquibaseUpdate

    val repositoryFactoryBuilder = new RepositoryFactoryBuilder()
    //val repositoryFactory = repositoryFactoryBuilder.createFactory

    // Create the repository proxy instance
    /*
    val podcastRepository: PodcastRepository = repositoryFactory.getRepository(classOf[PodcastRepository])
    val episodeRepository: EpisodeRepository = repositoryFactory.getRepository(classOf[EpisodeRepository])
    val feedRepository: FeedRepository = repositoryFactory.getRepository(classOf[FeedRepository])
    */

    val podcastService = new PodcastService(repositoryFactoryBuilder)
    val episodeService = new EpisodeService(repositoryFactoryBuilder)
    val feedService = new FeedService(repositoryFactoryBuilder)

    val em: EntityManager = repositoryFactoryBuilder.getEntityManager
    /*
    val emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory

    val podcastDao: PodcastDao = new PodcastDaoImpl(emf)
    val episodeDao: EpisodeDao =  new EpisodeDaoImpl(emf)
    val feedDao: FeedDao =  new FeedDaoImpl(emf)
    */

    /* TODO could I use this to run liquibase manually?
    val liquibase = new SpringLiquibase()
    liquibase.setDataSource(repositoryFactoryBuilder.getDataSource)
    liquibase.setChangeLog("classpath:db/liquibase/master.xml")
    */

    override def receive: Receive = {

        case ActorRefCrawlerActor(ref) => {
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
        }
        case ActorRefIndexStoreActor(ref) => {
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref
        }

        case ProposeNewFeed(feedUrl) => proposeFeed(feedUrl)

        case FeedStatusUpdate(feedUrl, timestamp, status) => updateFeed(feedUrl, timestamp, status)

        case UpdatePodcastMetadata(docId, podcast) => updatePodcast(docId, podcast)

        case UpdateEpisodeMetadata(podcastDocId, episode) => updateEpisode(podcastDocId, episode)

        // this is the case when an Episode has no iTunesImage URL set in the feed.
        // then we should set the image url of the whole podcast
        case UsePodcastItunesImage(echoId) => setEpisodesItunesImageToPodcast(echoId)

        case GetPodcast(echoId) => getPodcast(echoId)

        case GetAllPodcasts => getAllPodcasts

        case GetEpisode(echoId) => getEpisode(echoId)

        case GetEpisodesByPodcast(podcastId) => getEpisodesByPodcast(podcastId)

        case DebugPrintAllPodcasts => debugPrintAllPodcasts

        case DebugPrintAllEpisodes => debugPrintAllEpisodes

        case LoadTestFeeds => {
            log.info("Received LoadTestFeeds")

            val filename = "../feeds.txt"
            for (feed <- Source.fromFile(filename).getLines) {
                self ! ProposeNewFeed(feed)
            }
        }

    }

    @Transactional
    def proposeFeed(url: String) = {
        log.debug("Received msg proposing a new feed: " + url)

        /*
        Url62.create
        // 7NLCAyd6sKR7kDHxgAWFPG

        Url62.decode("7NLCAyd6sKR7kDHxgAWFPG")
        // c3587ec5-0976-497f-8374-61e0c2ea3da5

        Url62.encode(UUID.fromString("c3587ec5-0976-497f-8374-61e0c2ea3da5"))
        */

        // TODO check of feed is known yet
        // TODO handle known and unknown

        val tx = em.getTransaction
        tx.begin

        feedService.findOneByUrl(url).map(feed => {
            log.info("Proposed feed is already in database: {}", url)
            println(feed)
        }).getOrElse({

            val fakePodcastId = Url62.encode(UUID.randomUUID())

            var podcast = new PodcastDTO
            podcast.setEchoId(fakePodcastId)
            podcast.setTitle("<NOT YET PARSED>")
            podcast = podcastService.save(podcast)

            var feed = new FeedDTO
            feed.setUrl(url)
            feed.setLastChecked(LocalDateTime.now())
            feed.setLastStatus(FeedStatus.NEVER_CHECKED)
            feed.setPodcastId(podcast.getId)
            feed = feedService.save(feed)

            crawler ! FetchNewFeed(url, fakePodcastId)
        })

        tx.commit

    }

    @Transactional
    def updateFeed(url: String, timestamp: LocalDateTime, status: FeedStatus): Unit = {
        log.debug("Received FeedStatusUpdate({},{},{})", url, timestamp, status)

        val tx = em.getTransaction
        tx.begin

        feedService.findOneByUrl(url).map(feed => {
            feed.setLastChecked(timestamp)
            feed.setLastStatus(status)
            feedService.save(feed)
        }).getOrElse({
            log.error("Received UNKNOWN FEED/Podcast FeedStatusUpdate({},{},{})", url, timestamp, status)
        })

        tx.commit

    }

    @Transactional
    def updatePodcast(podcastId: String, podcastDTO: PodcastDTO): Unit = {
        log.debug("Received UpdatePodcastMetadata({},{})", podcastId, podcastDTO)

        val tx = em.getTransaction
        tx.begin

        val update: PodcastDTO = podcastService.findOneByEchoId(podcastId).map(p => {
            val updatedPodcast = podcastDTO
            updatedPodcast.setId(p.getId)
            updatedPodcast
        }).getOrElse({
            log.info("Received a UpdatePodcastMetadata for a podcast that is not yet in the database: {}", podcastId)
            podcastDTO
        })
        podcastService.save(update)

        tx.commit

    }

    @Transactional
    def updateEpisode(podcastId: String, episodeDTO: EpisodeDTO): Unit = {
        log.debug("Received UpdateEpisodeMetadata({},{})", podcastId, episodeDTO)

        val tx = em.getTransaction
        tx.begin

        podcastService.findOneByEchoId(podcastId).map(p => {
            val update: EpisodeDTO = episodeService.findOneByEchoId(episodeDTO.getEchoId).map(e => {
                val updatedEpisode = episodeDTO
                updatedEpisode.setId(e.getId)
                updatedEpisode
            }).getOrElse({
                episodeDTO
            })
            update.setPodcastId(p.getId)
            episodeService.save(update)
        }).getOrElse({
            log.error("No Podcast found in database with echoId={}", podcastId)
        })

        tx.commit

    }

    @Transactional
    def setEpisodesItunesImageToPodcast(episodeId: String): Unit = {
        log.debug("Received UsePodcastItunesImage({})", episodeId)

        val tx = em.getTransaction
        tx.begin

        episodeService.findOneByEchoId(episodeId).map(e => {
            val podcast = episodeService.findOne(e.getPodcastId)
            podcast.map(p => {
                e.setItunesImage(p.getItunesImage)
                episodeService.save(e)
            }).getOrElse({
                log.error("e.getPodcast produced null!")
            })
        }).getOrElse(
            log.error("Did not find Episode with echoId={} in the database (could not set its itunesImage therefore)", episodeId)
        )

        tx.commit

    }

    @Transactional
    def getPodcast(podcastId: String): Unit = {
        log.debug("Received GetPodcast('{}')", podcastId)

        val tx = em.getTransaction
        tx.begin

        podcastService.findOneByEchoId(podcastId).map(p => {
            sender ! PodcastResult(p)
        }).getOrElse({
            log.error("Database does not contain Podcast with echoId={}", podcastId)
            sender ! NoDocumentFound(podcastId)
        })

        tx.commit
    }

    @Transactional
    def getAllPodcasts: Unit = {
        log.debug("Received GetAllPodcasts()")

        val tx = em.getTransaction
        tx.begin

        val podcasts = podcastService.findAllWhereFeedStatusIsNot(FeedStatus.NEVER_CHECKED)
        sender ! AllPodcastsResult(podcasts)

        tx.commit

    }

    @Transactional
    def getEpisode(episodeId: String): Unit= {
        log.debug("Received GetEpisode('{}')", episodeId)

        val tx = em.getTransaction
        tx.begin

        episodeService.findOneByEchoId(episodeId).map(e => {
            sender ! EpisodeResult(e)
        }).getOrElse({
            log.error("Database does not contain Episode with echoId={}", episodeId)
            sender ! NoDocumentFound(episodeId)
        })

        tx.commit

    }

    @Transactional
    def getEpisodesByPodcast(podcastId: String): Unit = {
        log.debug("Received GetEpisodesByPodcast('{}')", podcastId)

        val tx = em.getTransaction
        tx.begin

        podcastService.findOneByEchoId(podcastId).map(p => {
            val episodes = episodeService.findAllByPodcast(p)
            sender ! EpisodesByPodcastResult(episodes)
        }).getOrElse({
            log.error("Database does not contain Podcast with echoId={}", podcastId)
            sender ! NoDocumentFound(podcastId)
        })

        tx.commit

    }

    def runLiquibaseUpdate: Unit = {
        log.info("Starting Liquibase update")
        try {
            Class.forName("org.h2.Driver");
            val conn: Connection = DriverManager.getConnection(
                "jdbc:h2:mem:echo;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                "sa",
                "");

            val database: Database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
            //database.setDefaultSchemaName("echo")

            val liquibase: Liquibase = new Liquibase("db/liquibase/master.xml", new ClassLoaderResourceAccessor(), database);

            val isDropFirst = true
            if (isDropFirst) {
                liquibase.dropAll
            }

            if(liquibase.isSafeToRunUpdate){
                liquibase.update(new Contexts(), new LabelExpression());
            } else {
                log.warning("Liquibase reports it is NOT safe to run the update")
            }

            log.info("Finished Liquibase update")
        } catch {
            case e: Exception => {
                log.error("Error on Liquibase update: {}", e)
            }
        }
    }

    def debugPrintAllPodcasts: Unit = {
        log.debug("Received DebugPrintAllPodcasts")
        log.info("All Podcasts in database:")

        val tx = em.getTransaction
        tx.begin

        println("------------------------")
        podcastService.findAll.map(p => println(p.getTitle))
        println("------------------------")

        tx.commit
    }

    def debugPrintAllEpisodes: Unit = {
        log.debug("Received DebugPrintAllEpisodes")
        log.info("All Episodes in database:")

        val tx = em.getTransaction
        tx.begin

        println("------------------------")
        episodeService.findAll.map(e => println(e.getTitle))
        println("------------------------")

        tx.commit
    }
}
