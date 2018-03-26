package echo.actor.directory

import java.time.LocalDateTime
import javax.persistence.{EntityManager, EntityManagerFactory}

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.directory.DirectoryProtocol._
import echo.actor.directory.repository.RepositoryFactoryBuilder
import echo.actor.directory.service._
import echo.actor.index.IndexProtocol.IndexStoreAddDoc
import echo.core.domain.dto._
import echo.core.domain.feed.FeedStatus
import echo.core.mapper._
import echo.core.util.ExoGenerator
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.transaction.support.TransactionSynchronizationManager

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */

class DirectoryStoreWorker(val workerIndex: Int,
                           val databaseUrl: String) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val MAX_PAGE_SIZE: Int = Option(CONFIG.getInt("echo.directory.max-page-size")).getOrElse(10000)

    private val exoGenerator: ExoGenerator = new ExoGenerator(workerIndex)

    private var crawler: ActorRef = _
    private var indexStore: ActorRef = _
    private var broker: ActorRef = _

    // I need this to run liquibase
    //val appCtx = new ClassPathXmlApplicationContext("application-context.xml")

    private val repositoryFactoryBuilder = new RepositoryFactoryBuilder(databaseUrl)
    //private val em: EntityManager = repositoryFactoryBuilder.getEntityManager
    private val emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory

    private val podcastService = new PodcastDirectoryService(log, repositoryFactoryBuilder)
    private val episodeService = new EpisodeDirectoryService(log, repositoryFactoryBuilder)
    private val feedService = new FeedDirectoryService(log, repositoryFactoryBuilder)
    private val chapterService = new ChapterDirectoryService(log, repositoryFactoryBuilder)

    private val podcastMapper = PodcastMapper.INSTANCE
    private val episodeMapper = EpisodeMapper.INSTANCE
    private val feedMapper = FeedMapper.INSTANCE
    private val chapterMapper = ChapterMapper.INSTANCE
    private val indexMapper = IndexMapper.INSTANCE
    private val nullMapper = NullMapper.INSTANCE

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

        case ActorRefDirectoryStoreActor(ref) =>
            log.debug("Received ActorRefDirectoryStoreActor(_)")
            broker = ref

        case ProposeNewFeed(feedUrl) => proposeFeed(feedUrl)

        case CheckPodcast(echoId) => onCheckPodcast(echoId)

        case CheckFeed(echoId) => onCheckFeed(echoId)

        case CheckAllPodcasts => onCheckAllPodcasts(0, MAX_PAGE_SIZE)

        case CheckAllFeeds => onCheckAllFeeds(0, MAX_PAGE_SIZE)

        case FeedStatusUpdate(podcastId, feedUrl, timestamp, status) => onFeedStatusUpdate(podcastId, feedUrl, timestamp, status)

        case SaveChapter(chapter) => onSaveChapter(chapter)

        case UpdatePodcast(echoId, url, podcast) => onUpdatePodcast(echoId, url, podcast)

        case UpdateEpisode(podcastExo, episode) => onUpdateEpisode(podcastExo, episode)

        // TODO
        //case UpdateFeed(podcastExo, feed) =>  ...
        //case UpdateChapter(episodeExo, chapter) =>  ...

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

                val podcastId = exoGenerator.getNewExo
                var podcast = ImmutablePodcastDTO.builder()
                    .setEchoId(podcastId)
                    .setTitle(podcastId)
                    .setDescription(url)
                    .setRegistrationComplete(false)
                    .setRegistrationTimestamp(LocalDateTime.now())
                    .create()

                podcastService.save(podcast).map(p => {

                    // broker ! UpdatePodcastMetadata(nullMapper.map(p)) // TODO

                    val feedId = exoGenerator.getNewExo
                    val feed = ImmutableFeedDTO.builder()
                        .setEchoId(feedId)
                        .setUrl(url)
                        .setLastChecked(LocalDateTime.now())
                        .setLastStatus(FeedStatus.NEVER_CHECKED)
                        .setPodcastId(p.getId)
                        .setRegistrationTimestamp(LocalDateTime.now())
                        .create()
                    feedService.save(feed).map(f => {
                        // crawler ! FetchFeedForNewPodcast(podcastId, f.getUrl)
                        crawler ! DownloadWithHeadCheck(podcastId, f.getUrl, NewPodcastFetchJob())

                        // broker ! UpdateFeed(nullMapper.map(f))
                    })
                })
            } else {
                log.info("Proposed feed is already in database: {}", url)
            }
        }
        doInTransaction(task, List(podcastService, feedService))
    }

    private def onFeedStatusUpdate(podcastId: String, url: String, timestamp: LocalDateTime, status: FeedStatus): Unit = {
        log.debug("Received FeedStatusUpdate({},{},{})", url, timestamp, status)
        def task = () => {
            feedService.findOneByUrlAndPodcastEchoId(url, podcastId).map(f => {
                val feed = feedMapper.toModifiable(f)
                feed.setLastChecked(timestamp)
                feed.setLastStatus(status)
                feedService.save(feed)
            }).getOrElse({
                log.error("Received UNKNOWN FEED/Podcast FeedStatusUpdate({},{},{})", url, timestamp, status)
            })
        }
        doInTransaction(task, List(feedService))
    }

    private def onSaveChapter(chapter: ChapterDTO): Unit = {
        log.debug("Received SaveChapter('{}') for episode : ", chapter.getTitle, chapter.getEpisodeExo)

        def task = () => {
            episodeService.findOneByEchoId(chapter.getEpisodeExo).map(e => {
                val c = chapterMapper.toModifiable(chapter)
                c.setEpisodeId(e.getId)
                chapterService.save(c)
            }).getOrElse({
                log.error("Could not save Chapter, no Episode found : {}", chapter.getEpisodeExo)
            })
        }
        doInTransaction(task, List(episodeService, chapterService))
    }

    private def onUpdatePodcast(podcastId: String, feedUrl: String, podcast: PodcastDTO): Unit = {
        log.debug("Received UpdatePodcast({},{},{})", podcastId, feedUrl, podcast.getEchoId)

        /* TODO
         * hier empfange ich die feedUrl die mir der Parser zurückgib, um anschließend die episode laden zu können
         * das würde ich mir gerne ersparen. dazu müsste ich aus der DB den "primärfeed" irgednwie bekommen können, also
         * jenen feed den ich immer benutze um updates zu laden
         */
        def task = () => {
            val update: ModifiablePodcastDTO = podcastService.findOneByEchoId(podcastId).map(p => {
                podcastMapper.update(podcast, p)
            }).getOrElse({
                log.debug("Podcast to update is not yet in database, therefore it will be added : {}", podcast.getEchoId)
                podcastMapper.toModifiable(podcast)
            })
            update.setRegistrationComplete(true)
            podcastService.save(update)

            // TODO we will fetch feeds for checking new episodes, but not because we updated podcast metadata
            // crawler ! FetchFeedForUpdateEpisodes(feedUrl, podcastId)
        }
        doInTransaction(task, List(podcastService))
    }

    private def onUpdateEpisode(podcastId: String, episode: EpisodeDTO): Unit = {
        log.debug("Received UpdateEpisode({},{})", podcastId, episode.getEchoId)
        def task = () => {
            podcastService.findOneByEchoId(podcastId).map(p => {
                val update: ModifiableEpisodeDTO = episodeService.findOneByEchoId(episode.getEchoId).map(e => {
                    episodeMapper.update(episode, e)
                }).getOrElse({
                    log.debug("Episode to update is not yet in database, therefore it will be added : {}", episode.getEchoId)
                    episodeMapper.toModifiable(episode)
                })
                update.setPodcastId(p.getId)

                // in case chapters were parsed, they were sent inside the episode, but we
                // must not save them with the episode in one pass, or they'll produce a
                // detached entity (because their episodes ID is yet unknown)
                //val chapters = update.getChapters.asScala
                //update.setChapters(null)

                //log.info("Persisting : {}", update) // TODO delete
                val saved = episodeService.save(update).get

                // TODO we'll have to check if an episode is yet known and in the database!
                // TODO best send a message to self to handle the chapter in a separate phase
                Option(saved.getChapters)
                    .foreach(_
                        .asScala
                        .map(c => chapterMapper.toModifiable(c))
                        .foreach(c => {
                            c.setEpisodeId(saved.getId)
                            c.setEpisodeExo(saved.getEchoId)
                            chapterService.save(c)
                        }))
            }).getOrElse({
                log.error("No Podcast found in database with EXO : {}", podcastId)
            })
        }
        doInTransaction(task, List(podcastService, episodeService, chapterService))
    }


    private def onUpdateFeedMetadataUrl(oldUrl: String, newUrl: String): Unit = {
        log.debug("Received UpdateFeedUrl('{}','{}')", oldUrl, newUrl)
        def task = () => {
            val feeds = feedService.findAllByUrl(oldUrl)
            if (feeds.nonEmpty) {
                feeds.foreach(f => {
                    val feed = feedMapper.toModifiable(f)
                    feed.setUrl(newUrl)
                    feedService.save(feed)
                })
            } else {
                log.error("No Feed found in database with url='{}'", oldUrl)
            }
        }
        doInTransaction(task, List(feedService))
    }

    private def onUpdateLinkByEchoId(echoId: String, newUrl: String): Unit = {
        log.debug("Received UpdateLinkByEchoId({},'{}')", echoId, newUrl)
        def task = () => {
            podcastService.findOneByEchoId(echoId).map(p => {
                val podcast = podcastMapper.toModifiable(p)
                podcast.setLink(newUrl)
                podcastService.save(podcast)
            }).getOrElse({
                episodeService.findOneByEchoId(echoId).map(e => {
                    val episode = episodeMapper.toModifiable(e)
                    episode.setLink(newUrl)
                    episodeService.save(episode)
                }).getOrElse({
                    log.error("Cannot update Link URL - no Podcast or Episode found by echo={}", echoId)
                })
            })
        }
        doInTransaction(task, List(podcastService,episodeService))
    }

    private def onGetPodcast(podcastId: String): Unit = {
        log.debug("Received GetPodcast('{}')", podcastId)
        def task = () => {
            podcastService.findOneByEchoId(podcastId).map(p => {
                Some(p)
            }).getOrElse({
                log.error("Database does not contain Podcast with echoId={}", podcastId)
                None
            })
        }
        doInTransaction(task, List(podcastService))
            .asInstanceOf[Option[PodcastDTO]]
            .map(p => {
                sender ! PodcastResult(nullMapper.clearImmutable(p))
            }).getOrElse({
                sender ! NothingFound(podcastId)
            })
    }

    private def onGetAllPodcasts(page: Int, size: Int): Unit = {
        log.debug("Received GetAllPodcasts({},{})", page, size)
        def task = () => {
            //val podcasts = podcastService.findAllWhereFeedStatusIsNot(FeedStatus.NEVER_CHECKED) // TODO broken
            podcastService.findAll(page, size)
        }
        val podcasts = doInTransaction(task, List(podcastService)).asInstanceOf[List[PodcastDTO]]
        sender ! AllPodcastsResult(podcasts.map(p => nullMapper.clearImmutable(p)))
    }

    private def onGetAllPodcastsRegistrationComplete(page: Int, size: Int): Unit = {
        log.debug("Received GetAllPodcastsRegistrationComplete({},{})", page, size)
        def task = () => {
            podcastService.findAllRegistrationCompleteAsTeaser(page, size)
        }
        val podcasts = doInTransaction(task, List(podcastService)).asInstanceOf[List[PodcastDTO]]
        sender ! AllPodcastsResult(podcasts.map(p => nullMapper.clearImmutable(p)))
    }

    private def onGetAllFeeds(page: Int, size: Int): Unit = {
        log.debug("Received GetAllFeeds({},{})", page, size)
        def task = () => {
            feedService.findAll(page, size)
        }
        val feeds = doInTransaction(task, List(feedService)).asInstanceOf[List[FeedDTO]]
        sender ! AllFeedsResult(feeds.map(f => nullMapper.clearImmutable(f)))
    }

    private def onGetEpisode(episodeId: String): Unit= {
        log.debug("Received GetEpisode('{}')", episodeId)
        def task = () => {
            episodeService.findOneByEchoId(episodeId).map(e => {
                Some(e)
            }).getOrElse({
                log.error("Database does not contain Episode with echoId={}", episodeId)
                None
            })
        }
        doInTransaction(task, List(episodeService))
            .asInstanceOf[Option[EpisodeDTO]]
            .map(e => {
                sender ! EpisodeResult(nullMapper.clearImmutable(e))
            }).getOrElse({
                sender ! NothingFound(episodeId)
            })
    }

    private def onGetEpisodesByPodcast(podcastId: String): Unit = {
        log.debug("Received GetEpisodesByPodcast('{}')", podcastId)

        def task = () => {
            episodeService.findAllByPodcastAsTeaser(podcastId)
        }
        val episodes = doInTransaction(task, List(episodeService)).asInstanceOf[List[EpisodeDTO]]
        sender ! EpisodesByPodcastResult(episodes.map(e => nullMapper.clearImmutable(e)))
    }

    private def onGetFeedsByPodcast(podcastId: String): Unit = {
        log.debug("Received GetFeedsByPodcast('{}')", podcastId)
        def task = () => {
            feedService.findAllByPodcast(podcastId)
        }
        val feeds = doInTransaction(task, List(feedService)).asInstanceOf[List[FeedDTO]]
        sender ! FeedsByPodcastResult(feeds.map(f => nullMapper.clearImmutable(f)))
    }

    private def onGetChaptersByEpisode(episodeId: String): Unit = {
        log.debug("Received GetChaptersByEpisode('{}')", episodeId)

        def task = () => {
            chapterService.findAllByEpisode(episodeId)
        }
        val chapters = doInTransaction(task, List(chapterService)).asInstanceOf[List[ChapterDTO]]
        sender ! ChaptersByEpisodeResult(chapters.map(c => nullMapper.clearImmutable(c)))
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

                    val e = episodeMapper.toModifiable(episode)

                    // generate a new episode echoId - the generator is (almost) ensuring uniqueness
                    e.setEchoId(exoGenerator.getNewExo)

                    podcastService.findOneByEchoId(podcastId).map(p => {
                        e.setPodcastId(p.getId)
                        e.setPodcastTitle(p.getTitle) // we'll not re-use this DTO, but extract the info again a bit further down

                        // check if the episode has a cover image defined, and set the one of the episode
                        Option(e.getImage).getOrElse({
                            // indexStore ! IndexStoreUpdateDocItunesImage(episode.getEchoId, p.getItunesImage)
                            e.setImage(p.getImage)
                        })
                    }).getOrElse({
                        log.error("No Podcast found with echoId : {}", podcastId)
                    })

                    e.setRegistrationTimestamp(LocalDateTime.now())
                    val result = episodeService.save(e)

                    // we must register the episodes chapters as well
                    //result.foreach(r => Option(r.getChapters).map(cs => chapterService.saveAll(r.getId, cs))) // TODO use the broker instead
                    /* TODO I have to broker the chapters to all stores, but if I sent those here,
                     * then they'll arrive before the episode arrives (the message is brokered
                     * further down the method) --> better send them in a Updateepisodewithchapters message
                    */
                    // TODO die chapters werden mit der episode mitgebrokert, brauche ich hier also nicht bereits vorab mit verschicken
                    /*
                    result.foreach(e => Option(episode.getChapters.asScala)
                        .filter(_ != null)
                        .map(_
                            .map(c => chapterMapper.toModifiable(c))
                            .foreach(c => {
                                c.setEpisodeExo(e.getEchoId)
                                broker ! SaveChapter(nullMapper.clearImmutable(c))
                            })
                        ))
                        */




                    // TODO why is this really necessary here?
                    // we'll need this info when we send the episode to the index in just a moment
                    //result.foreach(ep => ep.setPodcastTitle(e.getPodcastTitle))

                    // we already clean up all the IDs here, just for good manners. for the chapters,
                    // we simply reuse the chapters from since bevore saving the episode, because those yet lack an ID
                    result
                        .map(r => nullMapper.clearImmutable(r)
                        .withChapters(Option(r.getChapters)
                            .map(_
                                .asScala
                                .map(c => nullMapper.clearImmutable(c))
                                .asJava)
                            .orNull))
            }
        }

        val registeredEpisode: Option[EpisodeDTO] = doInTransaction(task, List(episodeService, podcastService, chapterService)).asInstanceOf[Option[EpisodeDTO]]

        // in case the episode was registered, we initiate some post processing
        registeredEpisode match {
            case Some(e) =>
                log.info("episode registered : '{}' [p:{},e:{}]", e.getTitle, podcastId, e.getEchoId)

                indexStore ! IndexStoreAddDoc(indexMapper.toImmutable(e))

                /* TODO send an update to all catalogs via the broker, so all other stores will have
                 * the data too (this will of course mean that I will update my own data, which is a
                 * bit pointless, by oh well... */
                broker ! UpdateEpisode(podcastId, nullMapper.clearImmutable(e))

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
    }

    private def debugPrintAllPodcasts(): Unit = {
        log.debug("Received DebugPrintAllPodcasts")
        log.info("All Podcasts in database:")
        def task = () => {
            podcastService.findAll(0, MAX_PAGE_SIZE).foreach(p => println(s"${p.getEchoId} : ${p.getTitle}"))
        }
        doInTransaction(task, List(podcastService))
    }

    private def debugPrintAllEpisodes(): Unit = {
        log.debug("Received DebugPrintAllEpisodes")
        log.info("All Episodes in database:")
        def task = () => {
            episodeService.findAll().foreach(e => println(s"${e.getEchoId} : ${e.getTitle}"))
        }
        doInTransaction(task, List(episodeService))
    }

    private def debugPrintAllFeeds(): Unit = {
        log.debug("Received DebugPrintAllFeeds")
        log.info("All Feeds in database:")
        def task = () => {
            feedService.findAll(0, MAX_PAGE_SIZE).foreach(f => println(s"${f.getEchoId} : ${f.getUrl}"))
        }
        doInTransaction(task, List(feedService))
    }

    private def debugPrintCountAllPodcasts(): Unit = {
        log.debug("Received DebugPrintCountAllPodcasts")
        def task = () => {
            podcastService.countAll()
        }
        val count = doInTransaction(task, List(podcastService))
        log.info("Podcasts in Database : {}", count)
    }

    private def debugPrintCountAllEpisodes(): Unit = {
        log.debug("Received DebugPrintCountAllEpisodes")
        def task = () => {
            episodeService.countAll()
        }
        val count = doInTransaction(task, List(episodeService))
        log.info("Episodes in Database : {}", count)
    }

    private def debugPrintCountAllFeeds(): Unit = {
        log.debug("Received DebugPrintCountAllFeeds")
        def task = () => {
            feedService.countAll()
        }
        val count = doInTransaction(task, List(feedService))
        log.info("Feeds in Database : {}", count)
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
