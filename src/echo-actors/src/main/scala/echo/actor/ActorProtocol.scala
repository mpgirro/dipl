package echo.actor

import java.time.LocalDateTime

import akka.actor.ActorRef
import echo.core.domain.dto._
import echo.core.domain.feed.FeedStatus


/**
  * @author Maximilian Irro
  */
object ActorProtocol {

    trait FetchJob
    case class NewPodcastFetchJob() extends FetchJob
    case class UpdateEpisodesFetchJob(etag: String, lastMod: String) extends FetchJob
    case class WebsiteFetchJob() extends FetchJob

    case class DownloadWithHeadCheck(echoId: String, url: String, job: FetchJob)
    case class DownloadContent(echoId: String, url: String, job: FetchJob, encoding: Option[String])

    // Crawler -> Parser
    case class ParseNewPodcastData(feedUrl: String, podcastId: String, feedData: String)
    case class ParseUpdateEpisodeData(feedUrl: String, podcastId: String, episodeFeedData: String)
    case class ParseWebsiteData(echoId: String, html: String)
    case class ParseFyydEpisodes(podcastId: String, episodesData: String)

    // Gateway(= Web) -> Searcher
    case class SearchRequest(query: String, page: Option[Int], size: Option[Int])

    // Searcher -> User
    case class SearchResults(results: ResultWrapperDTO)

    // These messages are sent to propagate actorRefs to other actors, to overcome circular dependencies
    case class ActorRefDirectoryStoreActor(ref: ActorRef)
    case class ActorRefCrawlerActor(ref: ActorRef)
    case class ActorRefParserActor(ref: ActorRef)
    case class ActorRefFeedStoreActor(ref: ActorRef)
    case class ActorRefIndexStoreActor(ref: ActorRef)
    case class ActorRefSearcherActor(ref: ActorRef)
    case class ActorRefGatewayActor(ref: ActorRef)

    // These are maintenance methods, I use during development
    case class DebugPrintAllPodcasts()    // User/CLI -> DirectoryStore
    case class DebugPrintAllEpisodes()    // User/CLI -> DirectoryStore
    case class DebugPrintAllFeeds()
    case class DebugPrintCountAllPodcasts()
    case class DebugPrintCountAllEpisodes()
    case class DebugPrintCountAllFeeds()
    case class LoadTestFeeds()            // CLI -> DirectoryStore
    case class LoadMassiveFeeds()         // CLI -> DirectoryStore

    // User -> Crawler
    // TODO: automatic: Crawler -> Crawler on a regular basis
    trait CrawlExternalDirectory
    case class CrawlFyyd(count: Int) extends CrawlExternalDirectory
    case class LoadFyydEpisodes(podcastId: String, fyydId: Long) extends CrawlExternalDirectory

    // CLI -> Master
    case class ShutdownSystem()

}
