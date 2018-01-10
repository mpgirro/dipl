package echo.actor.protocol

import java.time.LocalDateTime

import akka.actor.ActorRef
import echo.core.dto.document.{Document, EpisodeDocument, PodcastDocument}
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


    /* Crawler -> DirectoryStore
     *
     */
    case class FeedStatusUpdate(feedUrl: String, timestamp: LocalDateTime, status: FeedStatus)

    // Indexer -> DirectoryStore
    case class UpdatePodcastMetadata(docId: String, doc: PodcastDocument)
    case class UpdateEpisodeMetadata(podcastDocId: String, doc: EpisodeDocument)

    /* Crawler -> Indexer
     * the podcastDocId has to be there (even for new feeds)
     * the episodeDocIds may be empty for new feeds (all episodes are new)
     */
    case class IndexFeedData(feedUrl: String, podcastDocId: String, episodeDocIds: Array[String], feedData: String)

    /* Index -> Index
     *
     */
    case class IndexPodcastData(podcastDocId: String, podcastFeedData: String)

    /* Index -> Index
     *
     */
    case class IndexEpisodeData(episodeDocIds: Array[String], episodeFeedData: String)

    // Indexer -> IndexStore
    case class IndexStoreAddPodcast(podcast: Document)
    case class IndexStoreUpdatePodcast(podcast: Document)
    case class IndexStoreAddEpisode(episode: Document)
    case class IndexStoreUpdateEpisode(episode: Document)


    case class SearchRequest(query: String)                 // User -> Searcher
    case class SearchResults(results: Array[Document])      // Searcher -> User

    case class SearchIndex(query: String)                   // Searcher -> IndexStore

    // IndexStore -> Searcher
    trait IndexResult
    case class IndexResultsFound(query: String, results: Array[Document]) extends IndexResult
    case class NoIndexResultsFound(query: String) extends IndexResult

    // These messages are sent to propagate actorRefs to other actors, to overcome circular dependencies
    case class ActorRefDirectoryStoreActor(ref: ActorRef)
    case class ActorRefCrawlerActor(ref: ActorRef)
    case class ActorRefIndexerActor(ref: ActorRef)
    case class ActorRefFeedStoreActor(ref: ActorRef)
    case class ActorRefIndexStoreActor(ref: ActorRef)
    case class ActorRefSearcherActor(ref: ActorRef)

    // These are maintenance methods, I use during development
    case class DebugPrintAllDatabase()    // User -> DirectoryStore

    // User -> Crawler
    // TODO: automatic: Crawler -> Crawler on a regular basis
    trait CrawlExternalDirectory
    case class CrawlFyyd(count: Int) extends CrawlExternalDirectory

}
