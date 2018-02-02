package echo.actor.directory

import java.sql.{Connection, DriverManager}
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.EntityManager
import javax.transaction.Transactional

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.devskiller.friendly_id.Url62
import echo.actor.directory.repository.RepositoryFactoryBuilder
import echo.actor.directory.service.{EpisodeDirectoryService, FeedDirectoryService, PodcastDirectoryService}
import echo.actor.ActorProtocol._
import echo.core.model.dto.{EpisodeDTO, FeedDTO, PodcastDTO}
import echo.core.model.feed.FeedStatus
import liquibase.database.jvm.JdbcConnection
import liquibase.database.{Database, DatabaseFactory}
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.{Contexts, LabelExpression, Liquibase}

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

    runLiquibaseUpdate()

    val repositoryFactoryBuilder = new RepositoryFactoryBuilder()
    //val repositoryFactory = repositoryFactoryBuilder.createFactory

    // Create the repository proxy instance
    /*
    val podcastRepository: PodcastRepository = repositoryFactory.getRepository(classOf[PodcastRepository])
    val episodeRepository: EpisodeRepository = repositoryFactory.getRepository(classOf[EpisodeRepository])
    val feedRepository: FeedRepository = repositoryFactory.getRepository(classOf[FeedRepository])
    */

    val podcastService = new PodcastDirectoryService(repositoryFactoryBuilder)
    val episodeService = new EpisodeDirectoryService(repositoryFactoryBuilder)
    val feedService = new FeedDirectoryService(repositoryFactoryBuilder)

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

        case UpdatePodcastMetadata(echoId, podcast) => updatePodcast(echoId, podcast)

        case UpdateEpisodeMetadata(echoId, episode) => updateEpisode(echoId, episode)

        // this is the case when an Episode has no iTunesImage URL set in the feed.
        // then we should set the image url of the whole podcast
        case UsePodcastItunesImage(echoId) => setEpisodesItunesImageToPodcast(echoId)

        case GetPodcast(echoId) => onGetPodcast(echoId)

        case GetAllPodcasts => onGetAllPodcasts()

        case GetEpisode(echoId) => onGetEpisode(echoId)

        case GetEpisodesByPodcast(echoId) => onGetEpisodesByPodcast(echoId)

        case DebugPrintAllPodcasts => debugPrintAllPodcasts()

        case DebugPrintAllEpisodes => debugPrintAllEpisodes()

        case LoadTestFeeds => {
            log.info("Received LoadTestFeeds")

            val filename = "../feeds.txt"
            for (feed <- Source.fromFile(filename).getLines) {
                self ! ProposeNewFeed(feed)
            }
        }

    }

    @Transactional
    def proposeFeed(url: String): Unit = {
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
        tx.begin()

        feedService.findOneByUrl(url).map(feed => {
            log.info("Proposed feed is already in database: {}", url)
            println(feed)
        }).getOrElse({

            val fakePodcastId = Url62.encode(UUID.randomUUID())
            val podcast = new PodcastDTO
            podcast.setEchoId(fakePodcastId)
            podcast.setTitle("<NOT YET PARSED>")
            podcastService.save(podcast)

            val fakeFeedId = Url62.encode(UUID.randomUUID())
            val feed = new FeedDTO
            feed.setEchoId(fakeFeedId)
            feed.setUrl(url)
            feed.setLastChecked(LocalDateTime.now())
            feed.setLastStatus(FeedStatus.NEVER_CHECKED)
            feed.setPodcastId(podcast.getId)
            feedService.save(feed)

            crawler ! FetchNewFeed(url, fakePodcastId)
        })

        tx.commit()

    }

    @Transactional
    def updateFeed(url: String, timestamp: LocalDateTime, status: FeedStatus): Unit = {
        log.debug("Received FeedStatusUpdate({},{},{})", url, timestamp, status)

        val tx = em.getTransaction
        tx.begin()

        feedService.findOneByUrl(url).map(feed => {
            feed.setLastChecked(timestamp)
            feed.setLastStatus(status)
            feedService.save(feed)
        }).getOrElse({
            log.error("Received UNKNOWN FEED/Podcast FeedStatusUpdate({},{},{})", url, timestamp, status)
        })

        tx.commit()

    }

    @Transactional
    def updatePodcast(podcastId: String, podcast: PodcastDTO): Unit = {
        log.debug("Received UpdatePodcastMetadata({},{})", podcastId, podcast)

        val tx = em.getTransaction
        tx.begin()

        val update: PodcastDTO = podcastService.findOneByEchoId(podcastId).map(p => {
            podcast.setId(p.getId)
            podcast
        }).getOrElse({
            log.info("Received a UpdatePodcastMetadata for a podcast that is not yet in the database: {}", podcastId)
            podcast
        })
        podcastService.save(update)

        tx.commit()

    }

    @Transactional
    def updateEpisode(podcastId: String, episode: EpisodeDTO): Unit = {
        log.debug("Received UpdateEpisodeMetadata({},{})", podcastId, episode)

        val tx = em.getTransaction
        tx.begin()

        podcastService.findOneByEchoId(podcastId).map(p => {
            val updatedPodcast: EpisodeDTO = episodeService.findOneByEchoId(episode.getEchoId).map(e => {
                episode.setId(e.getId)
                episode
            }).getOrElse({
                episode
            })
            updatedPodcast.setPodcastId(p.getId)
            episodeService.save(updatedPodcast)
        }).getOrElse({
            log.error("No Podcast found in database with echoId={}", podcastId)
        })

        tx.commit()

    }

    @Transactional
    def setEpisodesItunesImageToPodcast(episodeId: String): Unit = {
        log.debug("Received UsePodcastItunesImage({})", episodeId)

        val tx = em.getTransaction
        tx.begin()

        episodeService.findOneByEchoId(episodeId).map(e => {
            val podcast = podcastService.findOne(e.getPodcastId) // TODO hier muss ich den podcastService aufrufen, Depp!
            podcast.map(p => {
                e.setItunesImage(p.getItunesImage)
                episodeService.save(e)
            }).getOrElse({
                log.error("e.getPodcast produced null!")
            })
        }).getOrElse(
            log.error("Did not find Episode with echoId={} in the database (could not set its itunesImage therefore)", episodeId)
        )

        tx.commit()

    }

    @Transactional
    def onGetPodcast(podcastId: String): Unit = {
        log.debug("Received GetPodcast('{}')", podcastId)

        val tx = em.getTransaction
        tx.begin()

        podcastService.findOneByEchoId(podcastId).map(p => {
            sender ! PodcastResult(p)
        }).getOrElse({
            log.error("Database does not contain Podcast with echoId={}", podcastId)
            sender ! NoDocumentFound(podcastId)
        })

        tx.commit()
    }

    @Transactional
    def onGetAllPodcasts(): Unit = {
        log.debug("Received GetAllPodcasts()")

        val tx = em.getTransaction
        tx.begin()

        //val podcasts = podcastService.findAllWhereFeedStatusIsNot(FeedStatus.NEVER_CHECKED) // TODO broken
        val podcasts = podcastService.findAll
        sender ! AllPodcastsResult(podcasts)

        tx.commit()

    }

    @Transactional
    def onGetEpisode(episodeId: String): Unit= {
        log.debug("Received GetEpisode('{}')", episodeId)

        val tx = em.getTransaction
        tx.begin()

        episodeService.findOneByEchoId(episodeId).map(e => {
            sender ! EpisodeResult(e)
        }).getOrElse({
            log.error("Database does not contain Episode with echoId={}", episodeId)
            sender ! NoDocumentFound(episodeId)
        })

        tx.commit()

    }

    @Transactional
    def onGetEpisodesByPodcast(podcastId: String): Unit = {
        log.debug("Received GetEpisodesByPodcast('{}')", podcastId)

        val tx = em.getTransaction
        tx.begin()

        podcastService.findOneByEchoId(podcastId).map(p => {
            val episodes = episodeService.findAllByPodcast(p)
            sender ! EpisodesByPodcastResult(episodes)
        }).getOrElse({
            log.error("Database does not contain Podcast with echoId={}", podcastId)
            sender ! NoDocumentFound(podcastId)
        })

        tx.commit()

    }

    def runLiquibaseUpdate(): Unit = {
        val startTime = System.currentTimeMillis
        try {
            Class.forName("org.h2.Driver");
            val conn: Connection = DriverManager.getConnection(
                "jdbc:h2:mem:echo;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                "sa",
                "");

            val database: Database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
            //database.setDefaultSchemaName("echo")

            val liquibase: Liquibase = new Liquibase("db/liquibase/master.xml", new ClassLoaderResourceAccessor(), database);

            val isDropFirst = true // TODO set this as a parameter
            if (isDropFirst) {
                liquibase.dropAll()
            }

            if(liquibase.isSafeToRunUpdate){
                liquibase.update(new Contexts(), new LabelExpression());
            } else {
                log.warning("Liquibase reports it is NOT safe to run the update")
            }
        } catch {
            case e: Exception => {
                log.error("Error on Liquibase update: {}", e)
            }
        } finally {
            val stopTime = System.currentTimeMillis
            val elapsedTime = stopTime - startTime
            log.info("Run Liquibase in {} ms", elapsedTime)
        }
    }

    def debugPrintAllPodcasts(): Unit = {
        log.debug("Received DebugPrintAllPodcasts")
        log.info("All Podcasts in database:")

        val tx = em.getTransaction
        tx.begin()

        println("------------------------")
        podcastService.findAll.foreach(p => println(p.getTitle))
        println("------------------------")

        tx.commit()
    }

    def debugPrintAllEpisodes(): Unit = {
        log.debug("Received DebugPrintAllEpisodes")
        log.info("All Episodes in database:")

        val tx = em.getTransaction
        tx.begin()

        println("------------------------")
        episodeService.findAll.foreach(e => println(e.getTitle))
        println("------------------------")

        tx.commit()
    }
}
