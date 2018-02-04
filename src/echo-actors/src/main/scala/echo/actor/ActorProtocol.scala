package echo.actor

import java.time.LocalDateTime

import akka.actor.ActorRef
import echo.core.model.dto.{EpisodeDTO, PodcastDTO, ResultWrapperDTO}
import echo.core.model.feed.FeedStatus

/**
  * @author Maximilian Irro
  */
object ActorProtocol {

    case class ProposeNewFeed(url: String) // sent from User to FeedStore

    // DirectoryStore -> Crawler
    case class FetchFeed(url: String, podcastId: String) // send from FeedStore to Crawler

    // Parser -> Crawler
    case class FetchWebsite(echoId: String, url: String)

    // Crawler -> DirectoryStore
    case class FeedStatusUpdate(feedUrl: String, timestamp: LocalDateTime, status: FeedStatus)

    // Parser -> DirectoryStore
    case class UpdatePodcastMetadata(podcastId: String, podcast: PodcastDTO)
    case class UpdateEpisodeMetadata(podcastId: String, episode: EpisodeDTO)

    // Crawler -> Parser
    case class ParseFeedData(feedUrl: String, podcastId: String, feedData: String)
    case class ParseWebsiteData(echoId: String, html: String)

    // Index -> Index
    case class ParsePodcastData(podcastId: String, podcastFeedData: String)
    case class ParseEpisodeData(episodeDocIds: List[String], episodeFeedData: String)

    // Parser -> IndexStore
    case class IndexStoreAddPodcast(podcast: PodcastDTO)
    case class IndexStoreUpdatePodcast(podcast: PodcastDTO)
    case class IndexStoreAddEpisode(episode: EpisodeDTO)
    case class IndexStoreUpdateEpisode(episode: EpisodeDTO)
    case class IndexStoreUpdateDocWebsiteData(echoId: String, html: String) // used for all document types

    // IndexStore -> IndexStore
    case class CommitIndex()

    // DirectoryStore -> IndexStore
    case class IndexStoreUpdateDocItunesImage(echoId: String, itunesImage: String)

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
    case class GetPodcast(echoId: String)
    case class GetAllPodcasts()
    case class GetEpisode(echoId: String)
    case class GetEpisodesByPodcast(echoId: String)

    // DirectoryStore -> Gateway
    trait DirectoryResult
    case class PodcastResult(podcast: PodcastDTO) extends DirectoryResult
    case class AllPodcastsResult(results: List[PodcastDTO]) extends DirectoryResult
    case class EpisodeResult(episode: EpisodeDTO) extends DirectoryResult
    case class EpisodesByPodcastResult(episodes: List[EpisodeDTO]) extends DirectoryResult
    case class NoDocumentFound(echoId: String) extends DirectoryResult

    // These are maintenance methods, I use during development
    case class DebugPrintAllPodcasts()    // User/CLI -> DirectoryStore
    case class DebugPrintAllEpisodes()    // User/CLI -> DirectoryStore
    case class LoadTestFeeds()            // (User/Web) Gateway -> DirectoryStore

    // User -> Crawler
    // TODO: automatic: Crawler -> Crawler on a regular basis
    trait CrawlExternalDirectory
    case class CrawlFyyd(count: Int) extends CrawlExternalDirectory

}
