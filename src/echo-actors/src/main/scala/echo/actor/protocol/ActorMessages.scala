package echo.actor.protocol

import java.time.LocalDateTime

import akka.actor.ActorRef
import echo.core.model.dto.{DTO, EpisodeDTO, PodcastDTO, ResultWrapperDTO}
import echo.core.model.feed.FeedStatus

import scala.collection.mutable.ListBuffer

/**
  * @author Maximilian Irro
  */
object ActorMessages {

    case class ProposeNewFeed(feedUrl: String) // sent from User to FeedStore

    /* DirectoryStore -> Crawler
     * DirectoryStore generated a new podcastDocId (the Feed URL?)
     */
    case class FetchNewFeed(feedUrl: String, podcastDocId: String) // send from FeedStore to Crawler

    /* DirectoryStore -> Crawler
     * DirectoryStore knows about the podcastDocId before hand, and about all Episodes currently known to this podcasts
     * (it does not matter if the episodes are currently in the feed to not)
     */
    case class FetchUpdateFeed(feedUrl: String, podcastDocId: String)

    // Parser -> Crawler
    case class FetchWebsite(echoId: String, url: String)

    /* Crawler -> DirectoryStore
     *
     */
    case class FeedStatusUpdate(feedUrl: String, timestamp: LocalDateTime, status: FeedStatus)

    // Parser -> DirectoryStore
    case class UpdatePodcastMetadata(docId: String, doc: PodcastDTO)
    case class UpdateEpisodeMetadata(podcastDocId: String, doc: EpisodeDTO)

    /* Crawler -> Parser
     * the podcastDocId has to be there (even for new feeds)
     * the episodeDocIds may be empty for new feeds (all episodes are new)
     */
    case class ParseFeedData(feedUrl: String, podcastDocId: String, feedData: String)

    // Crawler -> Parser
    case class ParseWebsiteData(echoId: String, html: String)

    /* Index -> Index
     *
     */
    case class ParsePodcastData(podcastDocId: String, podcastFeedData: String)

    /* Index -> Index
     *
     */
    case class ParseEpisodeData(episodeDocIds: List[String], episodeFeedData: String)

    // Parser -> IndexStore
    case class IndexStoreAddPodcast(podcast: PodcastDTO)
    case class IndexStoreUpdatePodcast(podcast: PodcastDTO)
    case class IndexStoreAddEpisode(episode: EpisodeDTO)
    case class IndexStoreUpdateEpisode(episode: EpisodeDTO)
    case class IndexSoreUpdateDocumentWebsiteData(echoId: String, websiteData: String) // used for all document types

    // DirectoryStore -> IndexStore
    case class IndexStoreUpdateEpisodeAddItunesImage(echoId: String, itunesImage: String)

    // Index -> DirectoryStore
    case class UsePodcastItunesImage(echoId: String)


    case class SearchRequest(query: String, page: Int, size: Int)                 // Gateway(= Web) -> Searcher
    case class SearchResults(results: ResultWrapperDTO)                           // Searcher -> User

    case class SearchIndex(query: String, page: Int, size: Int)                   // Searcher -> IndexStore

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
