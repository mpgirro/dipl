package echo.actor.directory

import java.time.LocalDateTime
import javax.persistence.{EntityManager, EntityManagerFactory}

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.directory.repository.RepositoryFactoryBuilder
import echo.actor.directory.service._
import echo.core.domain.dto.{ChapterDTO, EpisodeDTO, FeedDTO, PodcastDTO}
import echo.core.domain.feed.FeedStatus
import echo.core.mapper.IndexMapper
import echo.core.util.EchoIdGenerator
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.transaction.support.TransactionSynchronizationManager

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */

object DirectoryStore {

}

class DirectoryStore (val workerIndex: Int) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val MAX_PAGE_SIZE: Int = Option(CONFIG.getInt("echo.directory.max-page-size")).getOrElse(10000)

    private val idGenerator: EchoIdGenerator = new EchoIdGenerator(workerIndex)

    private var crawler: ActorRef = _
    private var indexStore: ActorRef = _

    // I need this to run liquibase
    //val appCtx = new ClassPathXmlApplicationContext("application-context.xml")

    private val repositoryFactoryBuilder = new RepositoryFactoryBuilder()
    //private val em: EntityManager = repositoryFactoryBuilder.getEntityManager
    private val emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory

    private val podcastService = new PodcastDirectoryService(log, repositoryFactoryBuilder)
    private val episodeService = new EpisodeDirectoryService(log, repositoryFactoryBuilder)
    private val feedService = new FeedDirectoryService(log, repositoryFactoryBuilder)
    private val chapterService = new ChapterDirectoryService(log, repositoryFactoryBuilder)

    /* TODO could I use this to run liquibase manually?
    val liquibase = new SpringLiquibase()
    liquibase.setDataSource(repositoryFactoryBuilder.getDataSource)
    liquibase.setChangeLog("classpath:db/liquibase/master.xml")
    */

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref

        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref

        case ProposeNewFeed(feedUrl) => proposeFeed(feedUrl)

        case CheckPodcast(echoId) => onCheckPodcast(echoId)

        case CheckFeed(echoId) => onCheckFeed(echoId)

        case CheckAllPodcasts => onCheckAllPodcasts(0, MAX_PAGE_SIZE)

        case CheckAllFeeds => onCheckAllFeeds(0, MAX_PAGE_SIZE)

        case FeedStatusUpdate(podcastId, feedUrl, timestamp, status) => onFeedStatusUpdate(podcastId, feedUrl, timestamp, status)

        case UpdatePodcastMetadata(echoId, url, podcast) => onUpdatePodcastMetadata(echoId, url, podcast)

        //case UpdateEpisodeMetadata(echoId, episode) => onUpdateEpisodeMetadata(echoId, episode)

        case UpdateFeedUrl(oldUrl, newUrl) => onUpdateFeedMetadataUrl(oldUrl, newUrl)

        case UpdateLinkByEchoId(echoId, newUrl) => onUpdateLinkByEchoId(echoId, newUrl)

        case GetPodcast(podcastId) => onGetPodcast(podcastId)

        case GetAllPodcasts(page, size) => onGetAllPodcasts(page, size)

        case GetAllPodcastsRegistrationComplete(page, size) => onGetAllPodcastsRegistrationComplete(page, size)

        case GetAllFeeds(page, size) => onGetAllFeeds(page, size)

        case GetEpisode(episodeId) => onGetEpisode(episodeId)

        case GetEpisodesByPodcast(podcastId) => onGetEpisodesByPodcast(podcastId)

        case GetFeedsByPodcast(podcastId) => onGetFeedsByPodcast(podcastId)

        case GetChaptersByEpisode(episodeId) => onGetChaptersByEpisode(episodeId)

        case RegisterEpisodeIfNew(podcastId, episode) => onRegisterEpisodeIfNew(podcastId, episode)

        case DebugPrintAllPodcasts => debugPrintAllPodcasts()

        case DebugPrintAllEpisodes => debugPrintAllEpisodes()

        case DebugPrintAllFeeds => debugPrintAllFeeds()

        case DebugPrintCountAllPodcasts => debugPrintCountAllPodcasts()

        case DebugPrintCountAllEpisodes => debugPrintCountAllEpisodes()

        case DebugPrintCountAllFeeds => debugPrintCountAllFeeds()

    }

    private def proposeFeed(url: String): Unit = {
        log.debug("Received msg proposing a new feed: " + url)

        def task = () => {
            if(feedService.findAllByUrl(url).isEmpty){

                // TODO for now we always create a podcast for an unknown feed, but we will have to check if the feed is an alternate to a known podcast

                val podcastId = idGenerator.getNewId
                var podcast = new PodcastDTO
                podcast.setEchoId(podcastId)
                podcast.setTitle(podcastId)
                podcast.setDescription(url)
                podcast.setRegistrationComplete(false)
                podcast.setRegistrationTimestamp(LocalDateTime.now())

                podcastService.save(podcast).map(p => {
                    val feedId = idGenerator.getNewId
                    val feed = new FeedDTO
                    feed.setEchoId(feedId)
                    feed.setUrl(url)
                    feed.setLastChecked(LocalDateTime.now())
                    feed.setLastStatus(FeedStatus.NEVER_CHECKED)
                    feed.setPodcastId(p.getId)
                    feed.setRegistrationTimestamp(LocalDateTime.now())
                    feedService.save(feed).map(f => {
                        // crawler ! FetchFeedForNewPodcast(podcastId, f.getUrl)
                        crawler ! DownloadWithHeadCheck(podcastId, f.getUrl, NewPodcastFetchJob())
                    })
                })
            } else {
                log.info("Proposed feed is already in database: {}", url)
            }
        }
        doInTransaction(task, List(podcastService, feedService))

        log.debug("Finished msg proposing a new feed: " + url)
    }

    private def onFeedStatusUpdate(podcastId: String, url: String, timestamp: LocalDateTime, status: FeedStatus): Unit = {
        log.debug("Received FeedStatusUpdate({},{},{})", url, timestamp, status)
        def task = () => {
            feedService.findOneByUrlAndPodcastEchoId(url, podcastId).map(feed => {
                feed.setLastChecked(timestamp)
                feed.setLastStatus(status)
                feedService.save(feed)
            }).getOrElse({
                log.error("Received UNKNOWN FEED/Podcast FeedStatusUpdate({},{},{})", url, timestamp, status)
            })
        }
        doInTransaction(task, List(feedService))

        log.debug("Finished FeedStatusUpdate({},{},{})", url, timestamp, status)
    }

    private def onUpdatePodcastMetadata(podcastId: String, feedUrl: String, podcast: PodcastDTO): Unit = {
        log.debug("Received UpdatePodcastMetadata({},{},{})", podcastId, feedUrl, podcast.getEchoId)

        /* TODO
         * hier empfange ich die feedUrl die mir der Parser zurückgib, um anschließend die episode laden zu können
         * das würde ich mir gerne ersparen. dazu müsste ich aus der DB den "primärfeed" irgednwie bekommen können, also
         * jenen feed den ich immer benutze um updates zu laden
         */
        def task = () => {
            val update: PodcastDTO = podcastService.findOneByEchoId(podcastId).map(p => {
                podcast.setId(p.getId)
                podcast.setRegistrationComplete(true)
                podcast
            }).getOrElse({
                log.error("Received a UpdatePodcastMetadata for a podcast that is not yet in the database (SHOULD THIS BE POSSIBLE?) : {}", podcastId)
                podcast
            })
            podcastService.save(update)

            // TODO we will fetch feeds for checking new episodes, but not because we updated podcast metadata
            // crawler ! FetchFeedForUpdateEpisodes(feedUrl, podcastId)
        }
        doInTransaction(task, List(podcastService))

        log.debug("Finished UpdatePodcastMetadata({},{},{})", podcastId, feedUrl, podcast.getEchoId)
    }

    /*
    @Deprecated
    private def onUpdateEpisodeMetadata(podcastId: String, episode: EpisodeDTO): Unit = {
        log.debug("Received UpdateEpisodeMetadata({},{})", podcastId, episode.getEchoId)
        def task = () => {
            podcastService.findOneByEchoId(podcastId).map(p => {
                val updatedEpisode: EpisodeDTO = episodeService.findOneByEchoId(episode.getEchoId).map(e => {
                    episode.setId(e.getId)
                    episode
                }).getOrElse({
                    // this is a not yet known episode
                    episode.setRegistrationTimestamp(LocalDateTime.now())
                    episode
                })
                updatedEpisode.setPodcastId(p.getId)

                // check if the episode has a cover image defined, and set the one of the episode
                Option(updatedEpisode.getItunesImage).getOrElse({
                    indexStore ! IndexStoreUpdateDocItunesImage(updatedEpisode.getEchoId, p.getItunesImage)
                    updatedEpisode.setItunesImage(p.getItunesImage)
                })

                episodeService.save(updatedEpisode)
            }).getOrElse({
                log.error("No Podcast found in database with echoId={}", podcastId)
            })
        }
        doInTransaction(task, List(podcastService, episodeService))

        log.debug("Finished UpdateEpisodeMetadata({},{})", podcastId, episode.getEchoId)
    }
    */

    private def onUpdateFeedMetadataUrl(oldUrl: String, newUrl: String): Unit = {
        log.debug("Received UpdateFeedUrl('{}','{}')", oldUrl, newUrl)
        def task = () => {
            val feeds = feedService.findAllByUrl(oldUrl)
            if(feeds.nonEmpty){
                feeds.foreach(f => {
                    f.setUrl(newUrl)
                    feedService.save(f)
                })
            } else {
                log.error("No Feed found in database with url='{}'", oldUrl)
            }
        }
        doInTransaction(task, List(feedService))

        log.debug("Finished UpdateFeedUrl('{}','{}')", oldUrl, newUrl)
    }

    private def onUpdateLinkByEchoId(echoId: String, newUrl: String): Unit = {
        log.debug("Received UpdateLinkByEchoId({},'{}')", echoId, newUrl)
        def task = () => {
            podcastService.findOneByEchoId(echoId).map(p => {
                p.setLink(newUrl)
                podcastService.save(p)
            }).getOrElse({
                episodeService.findOneByEchoId(echoId).map(e => {
                    e.setLink(newUrl)
                    episodeService.save(e)
                }).getOrElse({
                    log.error("Cannot update Link URL - no Podcast or Episode found by echo={}", echoId)
                })
            })
        }
        doInTransaction(task, List(podcastService,episodeService))

        log.debug("Finished UpdateLinkByEchoId({},'{}')", echoId, newUrl)
    }

    private def onGetPodcast(podcastId: String): Unit = {
        log.debug("Received GetPodcast('{}')", podcastId)
        def task = () => {
            podcastService.findOneByEchoId(podcastId).map(p => {
                sender ! PodcastResult(p)
            }).getOrElse({
                log.error("Database does not contain Podcast with echoId={}", podcastId)
                sender ! NothingFound(podcastId)
            })
        }
        doInTransaction(task, List(podcastService))

        log.debug("Finished GetPodcast('{}')", podcastId)
    }

    private def onGetAllPodcasts(page: Int, size: Int): Unit = {
        log.debug("Received GetAllPodcasts({},{})", page, size)
        def task = () => {
            //val podcasts = podcastService.findAllWhereFeedStatusIsNot(FeedStatus.NEVER_CHECKED) // TODO broken
            podcastService.findAll(page, size)
        }
        val podcasts = doInTransaction(task, List(podcastService)).asInstanceOf[List[PodcastDTO]]
        sender ! AllPodcastsResult(podcasts)

        log.debug("Finished GetAllPodcasts({},{})", page, size)
    }

    private def onGetAllPodcastsRegistrationComplete(page: Int, size: Int): Unit = {
        log.debug("Received GetAllPodcastsRegistrationComplete({},{})", page, size)
        def task = () => {
            podcastService.findAllRegistrationCompleteAsTeaser(page, size)
        }
        val podcasts = doInTransaction(task, List(podcastService)).asInstanceOf[List[PodcastDTO]]
        sender ! AllPodcastsResult(podcasts)

        log.debug("Finished GetAllPodcastsRegistrationComplete({},{})", page, size)
    }

    private def onGetAllFeeds(page: Int, size: Int): Unit = {
        log.debug("Received GetAllFeeds({},{})", page, size)
        def task = () => {
            feedService.findAll(page, size)
        }
        val feeds = doInTransaction(task, List(feedService)).asInstanceOf[List[FeedDTO]]
        sender ! AllFeedsResult(feeds)

        log.debug("Finished GetAllFeeds({},{})", page, size)
    }

    private def onGetEpisode(episodeId: String): Unit= {
        log.debug("Received GetEpisode('{}')", episodeId)
        def task = () => {
            episodeService.findOneByEchoId(episodeId).map(e => {
                sender ! EpisodeResult(e)
            }).getOrElse({
                log.error("Database does not contain Episode with echoId={}", episodeId)
                sender ! NothingFound(episodeId)
            })
        }
        doInTransaction(task, List(episodeService))

        log.debug("Finished GetEpisode('{}')", episodeId)
    }

    private def onGetEpisodesByPodcast(podcastId: String): Unit = {
        log.debug("Received GetEpisodesByPodcast('{}')", podcastId)

        def task = () => {
            episodeService.findAllByPodcastAsTeaser(podcastId)
        }
        val episodes = doInTransaction(task, List(episodeService)).asInstanceOf[List[EpisodeDTO]]
        sender ! EpisodesByPodcastResult(episodes)

        log.debug("Finished GetEpisodesByPodcast('{}')", podcastId)
    }

    private def onGetFeedsByPodcast(podcastId: String): Unit = {
        log.debug("Received GetFeedsByPodcast('{}')", podcastId)
        def task = () => {
            feedService.findAllByPodcast(podcastId)
        }
        val feeds = doInTransaction(task, List(feedService)).asInstanceOf[List[FeedDTO]]
        sender ! FeedsByPodcastResult(feeds)

        log.debug("Finished GetFeedsByPodcast('{}')", podcastId)
    }

    private def onGetChaptersByEpisode(episodeId: String): Unit = {
        log.debug("Received GetChaptersByEpisode('{}')", episodeId)

        def task = () => {
            chapterService.findAllByEpisode(episodeId)
        }
        val chapters = doInTransaction(task, List(chapterService)).asInstanceOf[List[ChapterDTO]]
        sender ! ChaptersByEpisodeResult(chapters)

        log.debug("Finished GetChaptersByEpisode('{}')", episodeId)
    }

    private def onCheckPodcast(podcastId: String): Unit = {
        log.debug("Received CheckPodcast({})", podcastId)
        def task = () => {
            // TODO hier muss ich irgendwie entscheiden, wass für einen feed ich nehme um zu updaten
            val feeds = feedService.findAllByPodcast(podcastId)
            if(feeds.nonEmpty){
                // crawler ! FetchFeedForUpdateEpisodes(podcastId, feeds.head.getUrl)
                val f = feeds.head
                crawler ! DownloadWithHeadCheck(podcastId, f.getUrl, UpdateEpisodesFetchJob(null, null)) // TODO set etag and lastMod
            } else {
                log.error("No Feeds registered for Podcast with echoId : {}", podcastId)
            }
        }
        doInTransaction(task, List(feedService))

        log.debug("Finished CheckPodcast({})", podcastId)
    }

    private def onCheckFeed(feedId: String): Unit = {
        log.debug("Received CheckFeed({})", feedId)
        def task = () => {
            // TODO hier muss ich irgendwie entscheiden, wass für einen feed ich nehme um zu updaten
            feedService.findOneByEchoId(feedId).map(f => {
                podcastService.findOneByFeed(feedId).map(p => {
                    //crawler ! FetchFeedForUpdateEpisodes(p.getEchoId, f.getUrl)
                    crawler ! DownloadWithHeadCheck(p.getEchoId, f.getUrl, UpdateEpisodesFetchJob(null, null)) // TODO set etag and lastMod
                }).getOrElse({
                    log.error("No Podcast found in Database for Feed with echoId : {}", feedId)
                })
            }).getOrElse({
                log.error("No Feed in Database with echoId : {}", feedId)
            })
        }
        doInTransaction(task, List(podcastService, feedService))

        log.debug("Finished CheckFeed({})", feedId)
    }

    private def onCheckAllPodcasts(page: Int, size: Int): Unit = {
        log.debug("Received CheckAllPodcasts({}, {})", page, size)

        def task = () => {
            // TODO hier muss ich irgendwie entscheiden, wass für einen feed ich nehme um zu updaten
            podcastService.findAll(page, size).foreach(p => {
                val feeds = feedService.findAllByPodcast(p.getEchoId)
                if(feeds.nonEmpty){
                    // crawler ! FetchFeedForUpdateEpisodes(p.getEchoId, feeds.head.getUrl) // TODO
                    val f = feeds.head
                    crawler ! DownloadWithHeadCheck(p.getEchoId, feeds.head.getUrl, UpdateEpisodesFetchJob(null, null)) // TODO set etag and lastMod
                } else {
                    log.error("No Feeds registered for Podcast with echoId : {}", p.getEchoId)
                }
            })
        }
        doInTransaction(task, List(podcastService, feedService))

        log.debug("Finished CheckAllPodcasts({}, {})", page, size)
    }

    private def onCheckAllFeeds(page: Int, size: Int): Unit = {
        log.debug("Received CheckAllFeeds({},{})", page, size)

        def task = () => {
            feedService.findAll(page, size).foreach(f => {
                podcastService.findOneByFeed(f.getEchoId).map{p => {
                    // crawler ! FetchFeedForUpdateEpisodes(p.getEchoId, f.getUrl) // TODO
                    crawler ! DownloadWithHeadCheck(p.getEchoId, f.getUrl, NewPodcastFetchJob())
                }}.getOrElse({
                    log.error("No Podcast found in Database for Feed with echoId : {}", f.getEchoId)
                })
            })
        }
        doInTransaction(task, List(podcastService, feedService))

        log.debug("Finished CheckAllFeeds({},{})", page, size)
    }

    private def onRegisterEpisodeIfNew(podcastId: String, episode: EpisodeDTO): Unit = {
        log.debug("Received RegisterEpisodeIfNew({}, '{}')", podcastId, episode.getTitle)

        def task: () => Option[EpisodeDTO] = () => {
            Option(episode.getGuid).map(guid => {
                episodeService.findAllByPodcastAndGuid(podcastId, guid).headOption
            }).getOrElse({
                episodeService.findOneByEnclosure(episode.getEnclosureUrl, episode.getEnclosureLength, episode.getEnclosureType)
            }) match {
                case Some(e) => None
                case None =>
                    // generate a new episode echoId - the generator is (almost) ensuring uniqueness
                    episode.setEchoId(idGenerator.getNewId)

                    podcastService.findOneByEchoId(podcastId).map(p => {
                        episode.setPodcastId(p.getId)
                        episode.setPodcastTitle(p.getTitle) // we'll not re-use this DTO, but extract the info again a bit further down

                        // check if the episode has a cover image defined, and set the one of the episode
                        Option(episode.getImage).getOrElse({
                            // indexStore ! IndexStoreUpdateDocItunesImage(episode.getEchoId, p.getItunesImage)
                            episode.setImage(p.getImage)
                        })
                    }).getOrElse({
                        log.error("No Podcast found with echoId : {}", podcastId)
                    })

                    episode.setRegistrationTimestamp(LocalDateTime.now())
                    val result = episodeService.save(episode)

                    // we must register the episodes chapters as well
                    result.foreach(e => Option(episode.getChapters).map(cs => chapterService.saveAll(e.getId, cs)))

                    // TODO why is this really necessary here?
                    // we'll need this info when we send the episode to the index in just a moment
                    result.foreach(e => e.setPodcastTitle(episode.getPodcastTitle))

                    result
            }
        }

        val registeredEpisode: Option[EpisodeDTO] = doInTransaction(task, List(episodeService, podcastService, chapterService)).asInstanceOf[Option[EpisodeDTO]]

        // in case the episode was registered, we initiate some post processing
        registeredEpisode match {
            case Some(e) =>
                log.info("episode registered : '{}' [p:{},e:{}]", e.getTitle, podcastId, e.getEchoId)

                indexStore ! IndexStoreAddDoc(IndexMapper.INSTANCE.map(e))

                // request that the website will get added to the episodes index entry as well
                Option(e.getLink) match {
                    case Some(link) =>
                        // crawler ! FetchWebsite(e.getEchoId, link)
                        crawler ! DownloadWithHeadCheck(e.getEchoId, link, WebsiteFetchJob())
                    case None => log.debug("No link set for episode {} --> no website data will be added to the index", episode.getEchoId)
                }
            case None =>
                log.debug("Episode is already registered : ('{}', {}, '{}')",episode.getEnclosureUrl, episode.getEnclosureLength, episode.getEnclosureType)
        }
        log.debug("Finished RegisterEpisodeIfNew()")
    }

    private def debugPrintAllPodcasts(): Unit = {
        log.debug("Received DebugPrintAllPodcasts")
        log.info("All Podcasts in database:")
        def task = () => {
            podcastService.findAll(0, MAX_PAGE_SIZE).foreach(p => println(s"${p.getEchoId} : ${p.getTitle}"))
        }
        doInTransaction(task, List(podcastService))

        log.debug("Finished DebugPrintAllPodcasts")
    }

    private def debugPrintAllEpisodes(): Unit = {
        log.debug("Received DebugPrintAllEpisodes")
        log.info("All Episodes in database:")
        def task = () => {
            episodeService.findAll().foreach(e => println(s"${e.getEchoId} : ${e.getTitle}"))
        }
        doInTransaction(task, List(episodeService))

        log.debug("Finished DebugPrintAllEpisodes")
    }

    private def debugPrintAllFeeds(): Unit = {
        log.debug("Received DebugPrintAllFeeds")
        log.info("All Feeds in database:")
        def task = () => {
            feedService.findAll(0, MAX_PAGE_SIZE).foreach(f => println(s"${f.getEchoId} : ${f.getUrl}"))
        }
        doInTransaction(task, List(feedService))

        log.debug("Finished DebugPrintAllFeeds")
    }

    private def debugPrintCountAllPodcasts(): Unit = {
        log.debug("Received DebugPrintCountAllPodcasts")
        def task = () => {
            podcastService.countAll()
        }
        val count = doInTransaction(task, List(podcastService))
        log.info("Podcasts in Database : {}", count)
        log.debug("Finished DebugPrintCountAllPodcasts")
    }

    private def debugPrintCountAllEpisodes(): Unit = {
        log.debug("Received DebugPrintCountAllEpisodes")
        def task = () => {
            episodeService.countAll()
        }
        val count = doInTransaction(task, List(episodeService))
        log.info("Episodes in Database : {}", count)
        log.debug("Finished DebugPrintCountAllEpisodes")
    }

    private def debugPrintCountAllFeeds(): Unit = {
        log.debug("Received DebugPrintCountAllFeeds")
        def task = () => {
            feedService.countAll()
        }
        val count = doInTransaction(task, List(feedService))
        log.info("Feeds in Database : {}", count)
        log.debug("Finished DebugPrintCountAllFeeds")
    }

    /**
      *
      * @param task the function to be executed inside a transaction
      * @param services all services used within the callable function, which therefore require a refresh before doing the work
      */
    private def doInTransaction(task: () => Any, services: List[DirectoryService] ): Any = {
        val em: EntityManager = emf.createEntityManager()
        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
            services.foreach(_.refresh(em))
            task()
        } finally {
            if(em.isOpen){
                em.close()
            }
            TransactionSynchronizationManager.unbindResource(emf)
        }
    }

}
