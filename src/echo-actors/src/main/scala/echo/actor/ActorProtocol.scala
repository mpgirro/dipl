package echo.actor

import java.time.LocalDateTime

import akka.actor.ActorRef
import echo.core.domain.dto._
import echo.core.domain.feed.{ChapterDTO, FeedStatus}


/**
  * @author Maximilian Irro
  */
object ActorProtocol {

    // Web/CLI -> DirectoryStore
    case class ProposeNewFeed(url: String)
    case class CheckPodcast(echoId: String)
    case class CheckFeed(echoId: String)
    case class CheckAllPodcasts()
    case class CheckAllFeeds()

    trait FetchJob
    case class NewPodcastFetchJob() extends FetchJob
    case class UpdateEpisodesFetchJob(etag: String, lastMod: String) extends FetchJob
    case class WebsiteFetchJob() extends FetchJob

    case class DownloadWithHeadCheck(echoId: String, url: String, job: FetchJob)
    case class DownloadContent(echoId: String, url: String, job: FetchJob)

    // Crawler -> DirectoryStore
    case class FeedStatusUpdate(podcastId: String, feedUrl: String, timestamp: LocalDateTime, status: FeedStatus)
    case class UpdateFeedUrl(oldUrl: String, newUrl: String)
    case class UpdateLinkByEchoId(echoId: String, newUrl: String)

    // Parser -> DirectoryStore
    case class UpdatePodcastMetadata(podcastId: String, feedUrl: String, podcast: PodcastDTO)
    //case class UpdateEpisodeMetadata(podcastId: String, episode: EpisodeDTO)

    // Questions: Parser -> DirectoryStore
    case class RegisterEpisodeIfNew(podcastId: String, episode: EpisodeDTO)

    // Crawler -> Parser
    case class ParseNewPodcastData(feedUrl: String, podcastId: String, feedData: String)
    case class ParseUpdateEpisodeData(feedUrl: String, podcastId: String, episodeFeedData: String)
    case class ParseWebsiteData(echoId: String, html: String)

    // Crawler/Parser/DirectoryStore -> IndexStore
    case class IndexStoreAddDoc(doc: IndexDocDTO) // from Parser with love
    case class IndexStoreUpdateDocWebsiteData(echoId: String, html: String)  // from Parser with love
    case class IndexStoreUpdateDocImage(echoId: String, itunesImage: String) // from DirectoryStore with love
    case class IndexStoreUpdateDocLink(echoId: String, newLink: String)      // from Crawler with love

    // IndexStore -> IndexStore
    case class CommitIndex()

    // Gateway(= Web) -> Searcher
    case class SearchRequest(query: String, page: Option[Int], size: Option[Int])

    // Searcher -> User
    case class SearchResults(results: ResultWrapperDTO)

    // Searcher -> IndexStore
    case class SearchIndex(query: String, page: Int, size: Int)

    // IndexStore -> Searcher
    trait IndexResult
    case class IndexResultsFound(query: String, results: ResultWrapperDTO) extends IndexResult
    case class NoIndexResultsFound(query: String) extends IndexResult

    // These messages are sent to propagate actorRefs to other actors, to overcome circular dependencies
    case class ActorRefDirectoryStoreActor(ref: ActorRef)
    case class ActorRefCrawlerActor(ref: ActorRef)
    case class ActorRefParserActor(ref: ActorRef)
    case class ActorRefFeedStoreActor(ref: ActorRef)
    case class ActorRefIndexStoreActor(ref: ActorRef)
    case class ActorRefSearcherActor(ref: ActorRef)
    case class ActorRefGatewayActor(ref: ActorRef)

    // Gateway -> DirectoryStore
    case class GetPodcast(podcastId: String)
    case class GetAllPodcasts(page: Int, size: Int)
    case class GetAllPodcastsRegistrationComplete(page: Int, size: Int)
    case class GetAllFeeds(page: Int, size: Int)
    case class GetEpisode(episodeId: String)
    case class GetEpisodesByPodcast(podcastId: String)
    case class GetFeedsByPodcast(podcastId: String)
    case class GetChaptersByEpisode(episodeId: String)

    // DirectoryStore -> Gateway
    trait DirectoryResult
    case class PodcastResult(podcast: PodcastDTO) extends DirectoryResult
    case class AllPodcastsResult(results: List[PodcastDTO]) extends DirectoryResult
    case class AllFeedsResult(results: List[FeedDTO]) extends DirectoryResult
    case class EpisodeResult(episode: EpisodeDTO) extends DirectoryResult
    case class EpisodesByPodcastResult(episodes: List[EpisodeDTO]) extends DirectoryResult
    case class FeedsByPodcastResult(feeds: List[FeedDTO]) extends DirectoryResult
    case class ChaptersByEpisodeResult(chapters: List[ChapterDTO]) extends DirectoryResult
    case class NothingFound(echoId: String) extends DirectoryResult

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

    // CLI -> Master
    case class ShutdownSystem()

}
