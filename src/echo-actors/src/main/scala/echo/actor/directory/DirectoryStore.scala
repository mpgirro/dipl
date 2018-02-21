package echo.actor.directory

import java.time.LocalDateTime
import javax.persistence.{EntityManager, EntityManagerFactory}

import akka.actor.{Actor, ActorLogging, ActorRef}
import echo.actor.ActorProtocol._
import echo.actor.directory.repository.RepositoryFactoryBuilder
import echo.actor.directory.service.{DirectoryService, EpisodeDirectoryService, FeedDirectoryService, PodcastDirectoryService}
import echo.core.domain.dto.{EpisodeDTO, FeedDTO, PodcastDTO}
import echo.core.domain.feed.FeedStatus
import echo.core.mapper.IndexMapper
import echo.core.util.EchoIdGenerator
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
  * @author Maximilian Irro
  */
class DirectoryStore extends Actor with ActorLogging {

    log.info("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

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

    /* TODO could I use this to run liquibase manually?
    val liquibase = new SpringLiquibase()
    liquibase.setDataSource(repositoryFactoryBuilder.getDataSource)
    liquibase.setChangeLog("classpath:db/liquibase/master.xml")
    */

    override def postStop: Unit = {
        log.info(s"${self.path.name} shut down")
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

        case CheckAllPodcasts => onCheckAllPodcasts()

        case CheckAllFeeds => onCheckAllFeeds()

        case FeedStatusUpdate(podcastId, feedUrl, timestamp, status) => onFeedStatusUpdate(podcastId, feedUrl, timestamp, status)

        case UpdatePodcastMetadata(echoId, url, podcast) => onUpdatePodcastMetadata(echoId, url, podcast)

        //case UpdateEpisodeMetadata(echoId, episode) => onUpdateEpisodeMetadata(echoId, episode)

        case UpdateFeedUrl(oldUrl, newUrl) => onUpdateFeedMetadataUrl(oldUrl, newUrl)

        case UpdateLinkByEchoId(echoId, newUrl) => onUpdateLinkByEchoId(echoId, newUrl)

        case GetPodcast(echoId) => onGetPodcast(echoId)

        case GetAllPodcasts => onGetAllPodcasts()

        case GetAllPodcastsRegistrationComplete => onGetAllPodcastsRegistrationComplete()

        case GetEpisode(echoId) => onGetEpisode(echoId)

        case GetEpisodesByPodcast(echoId) => onGetEpisodesByPodcast(echoId)

        //case IsEpisodeRegistered(enclosureUrl, enclosureLength, enclosureType) => onIsEpisodeRegistered(enclosureUrl, enclosureLength, enclosureType)

        case RegisterEpisodeIfNew(podcastId, episode) => onRegisterEpisodeIfNew(podcastId, episode)

        case DebugPrintAllPodcasts => debugPrintAllPodcasts()

        case DebugPrintAllEpisodes => debugPrintAllEpisodes()

        /*
        case LoadTestFeeds =>
            log.info("Received LoadTestFeeds")

            val filename = "../feeds.txt"
            for (feed <- Source.fromFile(filename).getLines) {
                self ! ProposeNewFeed(feed)
            }

        case LoadMassiveFeeds =>
            log.info("Received LoadMassiveFeeds")

            val filename = "../feeds_unique.txt"
            for (feed <- Source.fromFile(filename).getLines) {
                self ! ProposeNewFeed(feed)
            }
        */

    }

    private def proposeFeed(url: String): Unit = {
        log.debug("Received msg proposing a new feed: " + url)

        def task = () => {
            if(feedService.findAllByUrl(url).isEmpty){
                val podcastId: String = EchoIdGenerator.getNewId()
                var podcast = new PodcastDTO
                podcast.setEchoId(podcastId)
                podcast.setTitle(podcastId)
                podcast.setDescription(url)
                podcast.setRegistrationComplete(false)
                podcast.setRegistrationTimestamp(LocalDateTime.now())
                podcastService.save(podcast).map(p => {

                    val fakeFeedId = EchoIdGenerator.getNewId()
                    val feed = new FeedDTO
                    feed.setEchoId(fakeFeedId)
                    feed.setUrl(url)
                    feed.setLastChecked(LocalDateTime.now())
                    feed.setLastStatus(FeedStatus.NEVER_CHECKED)
                    feed.setPodcastId(p.getId)
                    feed.setRegistrationTimestamp(LocalDateTime.now())
                    feedService.save(feed)

                    crawler ! FetchFeedForNewPodcast(url, podcastId)
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
                sender ! NoDocumentFound(podcastId)
            })
        }
        doInTransaction(task, List(podcastService))

        log.debug("Finished GetPodcast('{}')", podcastId)
    }

    private def onGetAllPodcasts(): Unit = {
        log.debug("Received GetAllPodcasts()")
        def task = () => {
            //val podcasts = podcastService.findAllWhereFeedStatusIsNot(FeedStatus.NEVER_CHECKED) // TODO broken
            val podcasts = podcastService.findAll()
            sender ! AllPodcastsResult(podcasts)
        }
        doInTransaction(task, List(podcastService))

        log.debug("Finished GetAllPodcasts()")
    }

    private def onGetAllPodcastsRegistrationComplete(): Unit = {
        log.debug("Received GetAllPodcastsRegistrationComplete()")
        def task = () => {
            val podcasts = podcastService.findAllRegistrationCompleteAsTeaser()
            sender ! AllPodcastsResult(podcasts)
        }
        doInTransaction(task, List(podcastService))

        log.debug("Finished GetAllPodcastsRegistrationComplete()")
    }

    private def onGetEpisode(episodeId: String): Unit= {
        log.debug("Received GetEpisode('{}')", episodeId)
        def task = () => {
            episodeService.findOneByEchoId(episodeId).map(e => {
                sender ! EpisodeResult(e)
            }).getOrElse({
                log.error("Database does not contain Episode with echoId={}", episodeId)
                sender ! NoDocumentFound(episodeId)
            })
        }
        doInTransaction(task, List(episodeService))

        log.debug("Finished GetEpisode('{}')", episodeId)
    }

    private def onGetEpisodesByPodcast(podcastId: String): Unit = {
        log.debug("Received GetEpisodesByPodcast('{}')", podcastId)
        def task = () => {
            podcastService.findOneByEchoId(podcastId).map(p => {
                // TODO hier könnte ich den aufruf des Podcasts wegoptimieren
                val episodes = episodeService.findAllByPodcastAsTeaser(podcastId)
                sender ! EpisodesByPodcastResult(episodes)
            }).getOrElse({
                log.error("Database does not contain Podcast with echoId={}", podcastId)
                sender ! NoDocumentFound(podcastId)
            })
        }
        doInTransaction(task, List(podcastService, episodeService))

        log.debug("Finished GetEpisodesByPodcast('{}')", podcastId)
    }

    private def onCheckPodcast(podcastId: String): Unit = {
        log.debug("Received CheckPodcast({})", podcastId)
        def task = () => {
            // TODO hier muss ich irgendwie entscheiden, wass für einen feed ich nehme um zu updaten
            val feeds = feedService.findAllByPodcast(podcastId)
            if(feeds.nonEmpty){
                crawler ! FetchFeedForUpdateEpisodes(feeds.head.getUrl, podcastId)
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
                    crawler ! FetchFeedForUpdateEpisodes(f.getUrl, p.getEchoId)
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

    private def onCheckAllPodcasts(): Unit = {
        log.debug("Received CheckAllPodcasts()")

        def task = () => {
            // TODO hier muss ich irgendwie entscheiden, wass für einen feed ich nehme um zu updaten
            podcastService.findAll().foreach(p => {
                val feeds = feedService.findAllByPodcast(p.getEchoId)
                if(feeds.nonEmpty){
                    crawler ! FetchFeedForUpdateEpisodes(feeds.head.getUrl, p.getEchoId)
                } else {
                    log.error("No Feeds registered for Podcast with echoId : {}", p.getEchoId)
                }
            })
        }
        doInTransaction(task, List(podcastService, feedService))

        log.debug("Finished CheckAllPodcasts()")
    }

    private def onCheckAllFeeds(): Unit = {
        log.debug("Received CheckAllFeeds()")

        def task = () => {
            feedService.findAll().foreach(f => {
                podcastService.findOneByFeed(f.getEchoId).map{p => {
                    crawler ! FetchFeedForUpdateEpisodes(f.getUrl, p.getEchoId)
                }}.getOrElse({
                    log.error("No Podcast found in Database for Feed with echoId : {}", f.getEchoId)
                })
            })
        }
        doInTransaction(task, List(podcastService, feedService))

        log.debug("Finished CheckAllFeeds()")
    }

    /*
    private def onIsEpisodeRegistered(enclosureUrl: String, enclosureLength: Long, enclosureType: String): Unit = {
        log.debug("Received IsEpisodeRegistered('{}', {}, '{}')", enclosureUrl, enclosureLength, enclosureType)

        def task = () => {
            episodeService.findOneByEnlosure(enclosureUrl, enclosureLength, enclosureType).map(e => {
                sender ! EpisodeRegistered(e.getEchoId)
            }).getOrElse({
                sender ! EpisodeNotRegistered
            })
        }
        doInTransaction(task, List(episodeService))

        log.debug("Finished IsEpisodeRegistered()")
    }
    */

    private def onRegisterEpisodeIfNew(podcastId: String, episode: EpisodeDTO): Unit = {
        log.debug("Received RegisterEpisodeIfNew({}, '{}')", podcastId, episode.getTitle)

        def task: () => Option[EpisodeDTO] = () => {
            Option(episode.getGuid).map(guid => {
                episodeService.findOneByPodcastAndGuid(podcastId, guid)
            }).getOrElse({
                episodeService.findOneByEnclosure(episode.getEnclosureUrl, episode.getEnclosureLength, episode.getEnclosureType)
            }) match {
                case Some(e) => None
                case None =>
                    // generate a new episode echoId - the generator is (almost) ensuring uniqueness
                    episode.setEchoId(EchoIdGenerator.getNewId)

                    podcastService.findOneByEchoId(podcastId).map(p => {
                        episode.setPodcastId(p.getId)

                        // check if the episode has a cover image defined, and set the one of the episode
                        Option(episode.getItunesImage).getOrElse({
                            indexStore ! IndexStoreUpdateDocItunesImage(episode.getEchoId, p.getItunesImage)
                            episode.setItunesImage(p.getItunesImage)
                        })
                    }).getOrElse({
                        log.error("No Podcast found with echoId : {}", podcastId)
                    })

                    episode.setRegistrationTimestamp(LocalDateTime.now())
                    episodeService.save(episode)
            }
        }

        val registeredEpisode: Option[EpisodeDTO] = doInTransaction(task, List(episodeService, podcastService)).asInstanceOf[Option[EpisodeDTO]]

        // in case the episode was registered, we initiate some post processing
        registeredEpisode match {
            case Some(e) =>
                log.info("New Episode registered : '{}' [{},{}]", e.getTitle, podcastId, e.getEchoId)

                indexStore ! IndexStoreAddDoc(IndexMapper.INSTANCE.map(e))

                // request that the website will get added to the episodes index entry as well
                Option(e.getLink) match {
                    case Some(link) => crawler ! FetchWebsite(e.getEchoId, link)
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
            podcastService.findAll().foreach(p => println(s"${p.getEchoId} : ${p.getTitle}"))
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
