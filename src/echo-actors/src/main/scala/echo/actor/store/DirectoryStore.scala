package echo.actor.store

import java.sql.{Connection, DriverManager}
import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.devskiller.friendly_id.Url62
import echo.actor.directory.repository.{EpisodeRepository, FeedRepository, PodcastRepository, RepositoryFactoryBuilder}
import echo.actor.protocol.ActorMessages._
import echo.core.converter.mapper.{DateMapper, EpisodeMapper, PodcastMapper}
import echo.core.model.domain.{Episode, Feed, Podcast}
import echo.core.model.dto.{EpisodeDTO, FeedDTO, PodcastDTO}
import echo.core.model.feed.FeedStatus
import org.springframework.context.support.ClassPathXmlApplicationContext
import javax.persistence.{EntityManager, EntityManagerFactory, EntityTransaction}

import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.transaction.Transactional

import echo.actor.directory.orm.{EpisodeDao, FeedDao, PodcastDao}
import echo.actor.directory.orm.impl.{EpisodeDaoImpl, FeedDaoImpl, PodcastDaoImpl}
import echo.actor.directory.service.{EpisodeService, FeedService, PodcastService}
import liquibase.changelog.DatabaseChangeLog
import liquibase.{Contexts, LabelExpression, Liquibase}
import liquibase.database.jvm.JdbcConnection
import liquibase.database.{Database, DatabaseFactory}
import liquibase.integration.spring.SpringLiquibase
import liquibase.resource.ClassLoaderResourceAccessor
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.transaction.support.TransactionSynchronizationManager

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

    /*
    val emf = Persistence.createEntityManagerFactory("echo.core.model.domain", testProperties)
    val em = emf.createEntityManager
    */

    /*
    // Bind the same EntityManger used to create the Repository to the thread
    TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))

    try
        podcastRepository.save(someInstance) // Done in a transaction using 1 EntityManger

    finally {
        // Make sure to unbind when done with the repository instance
        TransactionSynchronizationManager.unbindResource(getEntityManagerFactory)
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

        case GetAllPodcasts => getAllPodcasts()

        case GetEpisode(echoId) => getEpisode(echoId)

        case GetEpisodesByPodcast(podcastId) => getEpisodesByPodcast(podcastId)

        case DebugPrintAllDatabase => {
            log.debug("Received DebugPrintAllDatabase")

            println("------------------------")
            println("All Podcasts in database")
            //podcastDao.getAll.map(p => println(p.getTitle))
            //podcastRepository.findAll().asScala.map(p => println(p.getTitle))
            podcastService.findAll().asScala.map(p => println(p.getTitle))
            println("------------------------")
        }

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

        /*
        Option(feedRepository.findOneByUrl(url)).map(feed => {
            log.info("Proposed feed is already in database: {}", url)
        }).getOrElse({

            val fakePodcastId = Url62.encode(UUID.randomUUID())

            // TODO for now we create 1 podcast for 1 feed, but in generall a new feed can be another of an already known podcast
            val podcastEntity = new Podcast
            podcastEntity.setEchoId(fakePodcastId)
            savePodcast(podcastEntity)
            //podcastRepository.save(podcastEntity)

            val feedEntity = new Feed
            feedEntity.setPodcast(podcastEntity)
            feedEntity.setUrl(url)
            feedEntity.setLastChecked(DateMapper.INSTANCE.asTimestamp(LocalDateTime.now()))
            feedEntity.setLastStatus(FeedStatus.NEVER_CHECKED)
            saveFeed(feedEntity)
            //feedRepository.save(feedEntity)

            crawler ! FetchNewFeed(url, fakePodcastId)
        })
        */

        /*
        feedDao.findByUrl(url).map(feed => {
            log.info("Proposed feed is already in database: {}", url)
        }).getOrElse({

            val fakePodcastId = Url62.encode(UUID.randomUUID())

            // TODO for now we create 1 podcast for 1 feed, but in generall a new feed can be another of an already known podcast
            val podcastEntity = new Podcast
            podcastEntity.setEchoId(fakePodcastId)
            podcastDao.save(podcastEntity)

            val feedEntity = new Feed
            feedEntity.setPodcast(podcastEntity)
            feedEntity.setUrl(url)
            feedEntity.setLastChecked(DateMapper.INSTANCE.asTimestamp(LocalDateTime.now()))
            feedEntity.setLastStatus(FeedStatus.NEVER_CHECKED)
            feedDao.save(feedEntity)

            crawler ! FetchNewFeed(url, fakePodcastId)
        })
        */

        val tx = em.getTransaction
        tx.begin

        Option(feedService.findOneByUrl(url)).map(feed => {
            log.info("Proposed feed is already in database: {}", url)
        }).getOrElse({

            val fakePodcastId = Url62.encode(UUID.randomUUID())

            var podcast = new PodcastDTO
            podcast.setEchoId(fakePodcastId)
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

        /*
        Option(feedRepository.findOneByUrl(url)).map(feed => {
            feed.setLastChecked(DateMapper.INSTANCE.asTimestamp(timestamp))
            feed.setLastStatus(status)
            saveFeed(feed)
            //feedRepository.save(feed)
        }).getOrElse({
            log.error("Received UNKNOWN FEED/Podcast FeedStatusUpdate({},{},{})", url, timestamp, status)
        })
        */

        /*
        feedDao.findByUrl(url).map(feed => {
            feed.setLastChecked(DateMapper.INSTANCE.asTimestamp(timestamp))
            feed.setLastStatus(status)
            feedDao.save(feed)
        }).getOrElse({
            log.error("Received UNKNOWN FEED/Podcast FeedStatusUpdate({},{},{})", url, timestamp, status)
        })
        */

        val tx = em.getTransaction
        tx.begin

        Option(feedService.findOneByUrl(url)).map(feed => {
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

        /*
        val update: Podcast = Option(podcastRepository.findOneByEchoId(podcastId)).map(p => {
            val updatedPodcast = PodcastMapper.INSTANCE.podcastDtoToPodcast(podcastDTO)
            updatedPodcast.setId(p.getId)
            updatedPodcast
        }).getOrElse({
            log.info("Received a UpdatePodcastMetadata for a podcast that is not yet in the database: {}", podcastId)
            PodcastMapper.INSTANCE.podcastDtoToPodcast(podcastDTO)
        })
        savePodcast(update)
        //podcastRepository.save(update)
        */

        /*
        val update: Podcast = podcastDao.findByEchoId(podcastId).map(p => {
            val updatedPodcast = PodcastMapper.INSTANCE.podcastDtoToPodcast(podcastDTO)
            updatedPodcast.setId(p.getId)
            updatedPodcast
        }).getOrElse({
            log.info("Received a UpdatePodcastMetadata for a podcast that is not yet in the database: {}", podcastId)
            PodcastMapper.INSTANCE.podcastDtoToPodcast(podcastDTO)
        })
        podcastDao.save(update)
        */

        val tx = em.getTransaction
        tx.begin

        val update: PodcastDTO = Option(podcastService.findOneByEchoId(podcastId)).map(p => {
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

        /*
        Option(podcastRepository.findOneByEchoId(podcastId)).map(p => {
            val update: Episode = Option(episodeRepository.findOneByEchoId(episodeDTO.getEchoId)).map(e => {
                val updatedEpisode = EpisodeMapper.INSTANCE.episodeDtoToEpisode(episodeDTO)
                updatedEpisode.setId(e.getId)
                updatedEpisode
            }).getOrElse({
                EpisodeMapper.INSTANCE.episodeDtoToEpisode(episodeDTO)
            })
            update.setPodcast(p)
            /* TODO do we need to do something here regarding owner stuff?
            p.getEpisodes.add(update)
            podcastDao.save(p)
            */
            saveEpisode(update)
            //episodeRepository.save(update)
        }).getOrElse({
            log.error("No Podcast found in database with echoId={}", podcastId)
        })
        */

        /*
        podcastDao.findByEchoId(podcastId).map(p => {
            val update: Episode = episodeDao.findByEchoId(episodeDTO.getEchoId).map(e => {
                val updatedEpisode = EpisodeMapper.INSTANCE.episodeDtoToEpisode(episodeDTO)
                updatedEpisode.setId(e.getId)
                updatedEpisode
            }).getOrElse({
                EpisodeMapper.INSTANCE.episodeDtoToEpisode(episodeDTO)
            })
            update.setPodcast(p)
            /* TODO do we need to do something here regarding owner stuff?
            p.getEpisodes.add(update)
            podcastDao.save(p)
            */
            episodeDao.save(update)
        }).getOrElse({
            log.error("No Podcast found in database with echoId={}", podcastId)
        })
        */

        val tx = em.getTransaction
        tx.begin

        Option(podcastService.findOneByEchoId(podcastId)).map(p => {
            val update: EpisodeDTO = Option(episodeService.findOneByEchoId(episodeDTO.getEchoId)).map(e => {
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

        /*
        Option(episodeRepository.findOneByEchoId(episodeId)).map(e => {
            val p = e.getPodcast
            if(p != null){
                e.setItunesImage(p.getItunesImage)
                saveEpisode(e)
                //episodeRepository.save(e)
            } else {
                log.error("e.getPodcast produced null!")
            }
        }).getOrElse(
            log.error("Did not find Episode with echoId={} in the database (could not set its itunesImage therefore)", episodeId)
        )
        */

        /*
        episodeDao.findByEchoId(episodeId).map(e => {
            val p = e.getPodcast
            if(p != null){
                e.setItunesImage(p.getItunesImage)
                episodeDao.save(e)
            } else {
                log.error("e.getPodcast produced null!")
            }
        }).getOrElse(
            log.error("Did not find Episode with echoId={} in the database (could not set its itunesImage therefore)", episodeId)
        )
        */

        val tx = em.getTransaction
        tx.begin

        Option(episodeService.findOneByEchoId(episodeId)).map(e => {
            val podcast = episodeService.findOne(e.getPodcastId)
            Option(podcast).map(p => {
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

        /*
        Option(podcastRepository.findOneByEchoId(podcastId)).map(p => {
            val podcast = PodcastMapper.INSTANCE.podcastToPodcastDto(p)
            sender ! PodcastResult(podcast)
        }).getOrElse({
            log.error("Database does not contain Podcast with echoId={}", podcastId)
            sender ! NoDocumentFound(podcastId)
        })
        */

        /*
        podcastDao.findByEchoId(podcastId).map(p => {
            val podcast = PodcastMapper.INSTANCE.podcastToPodcastDto(p)
            sender ! PodcastResult(podcast)
        }).getOrElse({
            log.error("Database does not contain Podcast with echoId={}", podcastId)
            sender ! NoDocumentFound(podcastId)
        })
        */

        val tx = em.getTransaction
        tx.begin

        Option(podcastService.findOneByEchoId(podcastId)).map(p => {
            sender ! PodcastResult(p)
        }).getOrElse({
            log.error("Database does not contain Podcast with echoId={}", podcastId)
            sender ! NoDocumentFound(podcastId)
        })

        tx.commit
    }

    @Transactional
    def getAllPodcasts(): Unit = {
        log.debug("Received GetAllPodcasts()")

        /*
        val podcasts = podcastRepository.findAll()
        val podcastDTOs = PodcastMapper.INSTANCE.podcastsToPodcastDtos(podcasts)
        sender ! AllPodcastsResult(podcastDTOs.asScala.toArray)
        */

        /*
        val podcasts = podcastDao.getAll
        val podcastDTOs = podcasts.map(p => PodcastMapper.INSTANCE.podcastToPodcastDto(p))
        sender ! AllPodcastsResult(podcastDTOs.toArray)
        */

        val tx = em.getTransaction
        tx.begin

        val podcasts = podcastService.findAll.asScala
        sender ! AllPodcastsResult(podcasts.toArray)

        tx.commit

    }

    @Transactional
    def getEpisode(episodeId: String): Unit= {
        log.debug("Received GetEpisode('{}')", episodeId)

        /*
        Option(episodeRepository.findOneByEchoId(episodeId)).map(e => {
            val episode = EpisodeMapper.INSTANCE.episodeToEpisodeDto(e)
            sender ! EpisodeResult(episode)
        }).getOrElse({
            log.error("Database does not contain Episode with echoId={}", episodeId)
            sender ! NoDocumentFound(episodeId)
        })
        */

        /*
        episodeDao.findByEchoId(episodeId).map(e => {
            val episode = EpisodeMapper.INSTANCE.episodeToEpisodeDto(e)
            sender ! EpisodeResult(episode)
        }).getOrElse({
            log.error("Database does not contain Episode with echoId={}", episodeId)
            sender ! NoDocumentFound(episodeId)
        })
        */

        val tx = em.getTransaction
        tx.begin

        Option(episodeService.findOneByEchoId(episodeId)).map(e => {
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

        /*
        Option(podcastRepository.findOneByEchoId(podcastId)).map(p => {
            val episodes = episodeRepository.findAllByPodcast(p)
            //val episodes = p.getEpisodes
            val episodeDTOs = EpisodeMapper.INSTANCE.episodesToEpisodesDtos(episodes)
            sender ! EpisodesByPodcastResult(episodeDTOs.asScala.toArray)
        }).getOrElse({
            log.error("Database does not contain Podcast with echoId={}", podcastId)
            sender ! NoDocumentFound(podcastId)
        })
        */

        /*
        podcastDao.findByEchoId(podcastId).map(p => {
            val episodes = episodeDao.getAllByPodcast(p).asJava
            //val episodes = p.getEpisodes
            val episodesDTOs = EpisodeMapper.INSTANCE.episodesToEpisodesDtos(episodes)
            sender ! EpisodesByPodcastResult(episodesDTOs.asScala.toArray)
        }).getOrElse({
            log.error("Database does not contain Podcast with echoId={}", podcastId)
            sender ! NoDocumentFound(podcastId)
        })
        */

        val tx = em.getTransaction
        tx.begin

        Option(podcastService.findOneByEchoId(podcastId)).map(p => {
            val episodes = episodeService.findAllByPodcast(p).asScala
            sender ! EpisodesByPodcastResult(episodes.toArray)
        }).getOrElse({
            log.error("Database does not contain Podcast with echoId={}", podcastId)
            sender ! NoDocumentFound(podcastId)
        })

        tx.commit

    }

    def runLiquibaseUpdate = {
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
}
