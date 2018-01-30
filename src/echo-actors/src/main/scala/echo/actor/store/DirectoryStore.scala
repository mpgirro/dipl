package echo.actor.store

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.devskiller.friendly_id.Url62
import echo.actor.directory.orm.{EpisodeDao, FeedDao, PodcastDao}
import echo.actor.directory.repository.{PodcastRepository, RepositoryFactoryBuilder}
import echo.actor.protocol.ActorMessages._
import echo.core.converter.mapper.{DateMapper, EpisodeMapper, PodcastMapper}
import echo.core.model.domain.{Episode, Feed, Podcast}
import echo.core.model.dto.{EpisodeDTO, PodcastDTO}
import echo.core.model.feed.FeedStatus
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class DirectoryStore extends Actor with ActorLogging {

    private var crawler: ActorRef = _
    private var indexStore: ActorRef = _

    //private var mockEchoIdGenerator = 0 // TODO replace with real ID gen

    val appCtx = new ClassPathXmlApplicationContext("application-context.xml")
    val podcastDao: PodcastDao = appCtx.getBean(classOf[PodcastDao])
    val episodeDao: EpisodeDao = appCtx.getBean(classOf[EpisodeDao])
    val feedDao: FeedDao = appCtx.getBean(classOf[FeedDao])


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

    val repositoryFactory = new RepositoryFactoryBuilder().createFactory

    // Create the repository proxy instance
    val podcastRepository: PodcastRepository = repositoryFactory.getRepository(classOf[PodcastRepository])

    override def receive: Receive = {

        case ActorRefCrawlerActor(ref) => {
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
        }
        case ActorRefIndexStoreActor(ref) => {
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref
        }

        case ProposeNewFeed(feedUrl) => {
            proposeFeed(feedUrl)
        }

        case FeedStatusUpdate(feedUrl, timestamp, status) => {
            updateFeed(feedUrl, timestamp, status)
        }

        case UpdatePodcastMetadata(docId: String, podcast: PodcastDTO) => {
            updatePodcast(docId, podcast)
        }

        case UpdateEpisodeMetadata(podcastDocId: String, episode: EpisodeDTO) => {
            updateEpisode(podcastDocId, episode)
        }

        // this is the case when an Episode has no iTunesImage URL set in the feed.
        // then we should set the image url of the whole podcast
        case UsePodcastItunesImage(echoId) => {
            setEpisodesItunesImageToPodcast(echoId)
        }


        case GetPodcast(echoId) => {
            getPodcast(echoId)
        }

        case GetAllPodcasts => {
            getAllPodcasts()
        }

        case GetEpisode(echoId) => {
            getEpisode(echoId)
        }

        case GetEpisodesByPodcast(podcastId) => {
            getEpisodesByPodcast(podcastId)
        }

        case DebugPrintAllDatabase => {
            log.debug("Received DebugPrintAllDatabase")

            println("------------------------")
            println("All Podcasts in database")
            //podcastDao.getAll.map(p => println(p.getTitle))
            podcastRepository.findAll().asScala.map(p => println(p.getTitle))
            println("------------------------")
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
    }

    @Transactional
    def updateFeed(url: String, timestamp: LocalDateTime, status: FeedStatus): Unit = {
        log.debug("Received FeedStatusUpdate({},{},{})", url, timestamp, status)

        feedDao.findByUrl(url).map(feed => {
            feed.setLastChecked(DateMapper.INSTANCE.asTimestamp(timestamp))
            feed.setLastStatus(status)
            feedDao.save(feed)
        }).getOrElse({
            log.error("Received UNKNOWN FEED/Podcast FeedStatusUpdate({},{},{})", url, timestamp, status)
        })
    }

    @Transactional
    def updatePodcast(podcastId: String, podcastDTO: PodcastDTO): Unit = {
        log.debug("Received UpdatePodcastMetadata({},{})", podcastId, podcastDTO)

        /* * * * * * * * * * * * * * * * * * *
         * TODO this is the new database code
         * * * * * * * * * * * * * * * * * * */
        val update: Podcast = podcastDao.findByEchoId(podcastId).map(p => {
            val updatedPodcast = PodcastMapper.INSTANCE.podcastDtoToPodcast(podcastDTO)
            updatedPodcast.setId(p.getId)
            updatedPodcast
        }).getOrElse({
            log.info("Received a UpdatePodcastMetadata for a podcast that is not yet in the database: {}", podcastId)
            PodcastMapper.INSTANCE.podcastDtoToPodcast(podcastDTO)
        })
        podcastDao.save(update)
    }

    @Transactional
    def updateEpisode(podcastId: String, episodeDTO: EpisodeDTO): Unit = {
        /* * * * * * * * * * * * * * * * * * *
         * TODO this is the new database code
         * * * * * * * * * * * * * * * * * * */
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
    }

    @Transactional
    def setEpisodesItunesImageToPodcast(episodeId: String): Unit = {
        log.debug("Received UsePodcastItunesImage({})", episodeId)

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
    }

    @Transactional(readOnly=true)
    def getPodcast(podcastId: String): Unit = {
        log.debug("Received GetPodcast('{}')", podcastId)

        podcastDao.findByEchoId(podcastId).map(p => {
            val podcast = PodcastMapper.INSTANCE.podcastToPodcastDto(p)
            sender ! PodcastResult(podcast)
        }).getOrElse({
            log.error("Database does not contain Podcast with echoId={}", podcastId)
            sender ! NoDocumentFound(podcastId)
        })
    }

    @Transactional(readOnly=true)
    def getAllPodcasts(): Unit = {
        log.debug("Received GetAllPodcasts()")

        val podcasts = podcastRepository.findAll()
        val podcastDTOs = PodcastMapper.INSTANCE.podcastsToPodcastDtos(podcasts)
        sender ! AllPodcastsResult(podcastDTOs.asScala.toArray)

        /*
        val podcasts = podcastDao.getAll
        val podcastDTOs = podcasts.map(p => PodcastMapper.INSTANCE.podcastToPodcastDto(p))
        sender ! AllPodcastsResult(podcastDTOs.toArray)
        */
    }

    @Transactional(readOnly=true)
    def getEpisode(episodeId: String): Unit= {
        log.debug("Received GetEpisode('{}')", episodeId)

        episodeDao.findByEchoId(episodeId).map(e => {
            val episode = EpisodeMapper.INSTANCE.episodeToEpisodeDto(e)
            sender ! EpisodeResult(episode)
        }).getOrElse({
            log.error("Database does not contain Episode with echoId={}", episodeId)
            sender ! NoDocumentFound(episodeId)
        })
    }

    def getEpisodesByPodcast(podcastId: String): Unit = {
        log.debug("Received GetEpisodesByPodcast('{}')", podcastId)

        podcastDao.findByEchoId(podcastId).map(p => {
            val episodes = episodeDao.getAllByPodcast(p).asJava
            //val episodes = p.getEpisodes
            val episodesDTOs = EpisodeMapper.INSTANCE.episodesToEpisodesDtos(episodes)
            sender ! EpisodesByPodcastResult(episodesDTOs.asScala.toArray)
        }).getOrElse({
            log.error("Database does not contain Podcast with echoId={}", podcastId)
            sender ! NoDocumentFound(podcastId)
        })
    }
}
