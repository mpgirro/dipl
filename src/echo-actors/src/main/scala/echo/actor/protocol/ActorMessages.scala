package echo.actor.protocol

import java.time.LocalDateTime

import akka.actor.ActorRef
import echo.core.dto.document.{DTO, EpisodeDTO, PodcastDTO}
import echo.core.feed.FeedStatus

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
    case class FetchUpdateFeed(feedUrl: String, podcastDocId: String, episodeDocIds: Array[String])

    // Indexer -> Crawler
    case class FetchWebsite(echoId: String, url: String)

    /* Crawler -> DirectoryStore
     *
     */
    case class FeedStatusUpdate(feedUrl: String, timestamp: LocalDateTime, status: FeedStatus)

    // Indexer -> DirectoryStore
    case class UpdatePodcastMetadata(docId: String, doc: PodcastDTO)
    case class UpdateEpisodeMetadata(podcastDocId: String, doc: EpisodeDTO)

    /* Crawler -> Indexer
     * the podcastDocId has to be there (even for new feeds)
     * the episodeDocIds may be empty for new feeds (all episodes are new)
     */
    case class IndexFeedData(feedUrl: String, podcastDocId: String, episodeDocIds: Array[String], feedData: String)

    // Crawler -> Indexer
    case class IndexWebsiteData(echoId: String, websiteData: String)

    /* Index -> Index
     *
     */
    case class IndexPodcastData(podcastDocId: String, podcastFeedData: String)

    /* Index -> Index
     *
     */
    case class IndexEpisodeData(episodeDocIds: Array[String], episodeFeedData: String)

    // Indexer -> IndexStore
    case class IndexStoreAddPodcast(podcast: PodcastDTO)
    case class IndexStoreUpdatePodcast(podcast: PodcastDTO)
    case class IndexStoreAddEpisode(episode: EpisodeDTO)
    case class IndexStoreUpdateEpisode(episode: EpisodeDTO)
    case class IndexSoreUpdateDocumentWebsiteData(echoId: String, websiteData: String) // used for all document types

    // DirectoryStore -> IndexStore
    case class IndexStoreUpdateEpisodeAddItunesImage(echoId: String, itunesImage: String)

    // Index -> DirectoryStore
    case class UsePodcastItunesImage(echoId: String)


    case class SearchRequest(query: String)                 // Gateway(= Web) -> Searcher
    case class SearchResults(results: Array[DTO])           // Searcher -> User

    case class SearchIndex(query: String)                   // Searcher -> IndexStore

    // IndexStore -> Searcher
    trait IndexResult
    case class IndexResultsFound(query: String, results: Array[DTO]) extends IndexResult
    case class NoIndexResultsFound(query: String) extends IndexResult

    // These messages are sent to propagate actorRefs to other actors, to overcome circular dependencies
    case class ActorRefDirectoryStoreActor(ref: ActorRef)
    case class ActorRefCrawlerActor(ref: ActorRef)
    case class ActorRefIndexerActor(ref: ActorRef)
    case class ActorRefFeedStoreActor(ref: ActorRef)
    case class ActorRefIndexStoreActor(ref: ActorRef)
    case class ActorRefSearcherActor(ref: ActorRef)

    // Gateway -> DirectoryStore
    case class GetPodcast(echoId: String)
    case class GetEpisode(echoId: String)

    // DirectoryStore -> Gateway
    trait DirectoryResult
    case class PodcastResult(podcast: PodcastDTO) extends DirectoryResult
    case class EpisodeResult(episode: EpisodeDTO) extends DirectoryResult
    case class NoDocumentFound(echoId: String) extends DirectoryResult

    // These are maintenance methods, I use during development
    case class DebugPrintAllDatabase()    // User -> DirectoryStore

    // User -> Crawler
    // TODO: automatic: Crawler -> Crawler on a regular basis
    trait CrawlExternalDirectory
    case class CrawlFyyd(count: Int) extends CrawlExternalDirectory

}
