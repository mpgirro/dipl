package echo.actor.directory

import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.{EntityManager, EntityManagerFactory}

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.devskiller.friendly_id.Url62
import echo.actor.ActorProtocol._
import echo.actor.directory.repository.RepositoryFactoryBuilder
import echo.actor.directory.service.{EpisodeDirectoryService, FeedDirectoryService, PodcastDirectoryService}
import echo.core.model.dto.{EpisodeDTO, FeedDTO, PodcastDTO}
import echo.core.model.feed.FeedStatus
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.transaction.support.TransactionSynchronizationManager

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

    private val repositoryFactoryBuilder = new RepositoryFactoryBuilder()
    private val em: EntityManager = repositoryFactoryBuilder.getEntityManager
    private val emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory

    private val podcastService = new PodcastDirectoryService(log, repositoryFactoryBuilder)
    private val episodeService = new EpisodeDirectoryService(log, repositoryFactoryBuilder)
    private val feedService = new FeedDirectoryService(log, repositoryFactoryBuilder)

    /* TODO could I use this to run liquibase manually?
    val liquibase = new SpringLiquibase()
    liquibase.setDataSource(repositoryFactoryBuilder.getDataSource)
    liquibase.setChangeLog("classpath:db/liquibase/master.xml")
    */

    override def receive: Receive = {

        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref

        case ProposeNewFeed(feedUrl) => proposeFeed(feedUrl)

        case FeedStatusUpdate(feedUrl, timestamp, status) => onFeedStatusUpdate(feedUrl, timestamp, status)

        case UpdatePodcastMetadata(echoId, podcast) => onUpdatePodcastMetadata(echoId, podcast)

        case UpdateEpisodeMetadata(echoId, episode) => onUpdateEpisodeMetadata(echoId, episode)

        // this is the case when an Episode has no iTunesImage URL set in the feed.
        // then we should set the image url of the whole podcast
        case UsePodcastItunesImage(echoId) => setEpisodesItunesImageToPodcast(echoId)

        case GetPodcast(echoId) => onGetPodcast(echoId)

        case GetAllPodcasts => onGetAllPodcasts()

        case GetEpisode(echoId) => onGetEpisode(echoId)

        case GetEpisodesByPodcast(echoId) => onGetEpisodesByPodcast(echoId)

        case DebugPrintAllPodcasts => debugPrintAllPodcasts()

        case DebugPrintAllEpisodes => debugPrintAllEpisodes()

        case LoadTestFeeds =>
            log.info("Received LoadTestFeeds")

            val filename = "../feeds.txt"
            for (feed <- Source.fromFile(filename).getLines) {
                self ! ProposeNewFeed(feed)
            }

    }

    private def proposeFeed(url: String): Unit = {
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

        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
            feedService.findOneByUrl(url).map(feed => {
                log.info("Proposed feed is already in database: {}", url)
                println(feed)
            }).getOrElse({
                val fakePodcastId = Url62.encode(UUID.randomUUID())
                var podcast = new PodcastDTO
                podcast.setEchoId(fakePodcastId)
                //podcast.setTitle("<NOT YET PARSED>")
                podcast.setTitle(url)
                podcast.setDescription(url) // for debugging
                podcastService.save(podcast).map(p => {
                    val fakeFeedId = Url62.encode(UUID.randomUUID())
                    val feed = new FeedDTO
                    feed.setEchoId(fakeFeedId)
                    feed.setUrl(url)
                    feed.setLastChecked(LocalDateTime.now())
                    feed.setLastStatus(FeedStatus.NEVER_CHECKED)
                    feed.setPodcastId(podcast.getId)
                    feedService.save(feed)

                    crawler ! FetchFeed(url, fakePodcastId)
                })
            })
        } finally {
            // Make sure to unbind when done with the repository instance
            TransactionSynchronizationManager.unbindResource(emf)
        }
    }

    private def onFeedStatusUpdate(url: String, timestamp: LocalDateTime, status: FeedStatus): Unit = {
        log.debug("Received FeedStatusUpdate({},{},{})", url, timestamp, status)

        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
            feedService.findOneByUrl(url).map(feed => {
                feed.setLastChecked(timestamp)
                feed.setLastStatus(status)
                feedService.save(feed)
            }).getOrElse({
                log.error("Received UNKNOWN FEED/Podcast FeedStatusUpdate({},{},{})", url, timestamp, status)
            })
        } finally {
            TransactionSynchronizationManager.unbindResource(emf)
        }
    }

    private def onUpdatePodcastMetadata(podcastId: String, podcast: PodcastDTO): Unit = {
        log.debug("Received UpdatePodcastMetadata({},{})", podcastId, podcast.getEchoId)

        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
            val update: PodcastDTO = podcastService.findOneByEchoId(podcastId).map(p => {
                podcast.setId(p.getId)
                podcast
            }).getOrElse({
                log.info("Received a UpdatePodcastMetadata for a podcast that is not yet in the database: {}", podcastId)
                podcast
            })
            podcastService.save(update)
        } finally {
            TransactionSynchronizationManager.unbindResource(emf)
        }
    }

    private def onUpdateEpisodeMetadata(podcastId: String, episode: EpisodeDTO): Unit = {
        log.debug("Received UpdateEpisodeMetadata({},{})", podcastId, episode.getEchoId)

        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
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
        } finally {
            TransactionSynchronizationManager.unbindResource(emf)
        }
    }

    private def setEpisodesItunesImageToPodcast(episodeId: String): Unit = {
        log.debug("Received UsePodcastItunesImage({})", episodeId)

        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
            episodeService.findOneByEchoId(episodeId).map(e => {
                val podcast = podcastService.findOne(e.getPodcastId) // TODO hier muss ich den podcastService aufrufen, Depp!
                podcast.map(p => {
                    e.setItunesImage(p.getItunesImage)
                    episodeService.save(e)

                    indexStore ! IndexStoreUpdateDocItunesImage(episodeId, p.getItunesImage)
                }).getOrElse({
                    log.error("e.getPodcast produced null!")
                })
            }).getOrElse(
                log.error("Did not find Episode with echoId={} in the database (could not set its itunesImage therefore)", episodeId)
            )
        } finally {
            TransactionSynchronizationManager.unbindResource(emf)
        }
    }

    private def onGetPodcast(podcastId: String): Unit = {
        log.debug("Received GetPodcast('{}')", podcastId)

        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
            podcastService.findOneByEchoId(podcastId).map(p => {
                sender ! PodcastResult(p)
            }).getOrElse({
                log.error("Database does not contain Podcast with echoId={}", podcastId)
                sender ! NoDocumentFound(podcastId)
            })
        } finally {
            TransactionSynchronizationManager.unbindResource(emf)
        }
    }

    private def onGetAllPodcasts(): Unit = {
        log.debug("Received GetAllPodcasts()")

        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
            //val podcasts = podcastService.findAllWhereFeedStatusIsNot(FeedStatus.NEVER_CHECKED) // TODO broken
            val podcasts = podcastService.findAll()
            sender ! AllPodcastsResult(podcasts)
        } finally {
            TransactionSynchronizationManager.unbindResource(emf)
        }
    }

    private def onGetEpisode(episodeId: String): Unit= {
        log.debug("Received GetEpisode('{}')", episodeId)

        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
            episodeService.findOneByEchoId(episodeId).map(e => {
                sender ! EpisodeResult(e)
            }).getOrElse({
                log.error("Database does not contain Episode with echoId={}", episodeId)
                sender ! NoDocumentFound(episodeId)
            })
        } finally {
            TransactionSynchronizationManager.unbindResource(emf)
        }
    }

    private def onGetEpisodesByPodcast(podcastId: String): Unit = {
        log.debug("Received GetEpisodesByPodcast('{}')", podcastId)

        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
            podcastService.findOneByEchoId(podcastId).map(p => {
                val episodes = episodeService.findAllByPodcast(p)
                sender ! EpisodesByPodcastResult(episodes)
            }).getOrElse({
                log.error("Database does not contain Podcast with echoId={}", podcastId)
                sender ! NoDocumentFound(podcastId)
            })
        } finally {
            TransactionSynchronizationManager.unbindResource(emf)
        }
    }



    private def debugPrintAllPodcasts(): Unit = {
        log.debug("Received DebugPrintAllPodcasts")
        log.info("All Podcasts in database:")

        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(emf.createEntityManager()))
        try {
            podcastService.findAll().foreach(p => println(p.getTitle))
        } finally {
            TransactionSynchronizationManager.unbindResource(emf)
        }
    }

    private def debugPrintAllEpisodes(): Unit = {
        log.debug("Received DebugPrintAllEpisodes")
        log.info("All Episodes in database:")

        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(emf.createEntityManager()))
        try {
            episodeService.findAll().foreach(e => println(e.getTitle))
        } finally {
            TransactionSynchronizationManager.unbindResource(emf)
        }
    }
}
