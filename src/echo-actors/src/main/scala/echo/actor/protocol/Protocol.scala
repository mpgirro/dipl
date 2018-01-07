package echo.actor.protocol

import java.time.LocalDateTime

import echo.core.dto.document.Document
import echo.core.feed.FeedStatus

import scala.collection.mutable.ListBuffer

/**
  * @author Maximilian Irro
  */
object Protocol {

    /*
    trait CrawlerMessage

    case class CrawlFeed(feed : String) extends CrawlerMessage


    trait IndexMessage

    case class ProcessPodcastFeedData(feedData : String) extends IndexMessage
    case class ProcessEpisodeFeedData(feedRef : String, episodeData : String) extends IndexMessage

    case class AddPodcastToIndex(podcast : String) extends IndexMessage
    case class AddEpisodeToIndex(podcast : String, episode : String) extends IndexMessage

    case class QueryIndexForPodcast(query : String) extends IndexMessage
    case class SearchResultForPodcastEpisodes(query : String, answer : ListBuffer[String]) extends IndexMessage


    trait SearchMessage

    case class SearchQuery(query: String) // send from User to SearcherActor
    case class SearchIndex(query: String) // send from SearcherActor to IndexStore


    trait SearchReply

    case class SearchResultsFound(query: String, results: Array[Document])
    case class NoSearchResultsFound(query: String)
    */


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
    case class FeedStatusUpdate(feedUrl: String, timestamp: LocalDateTime, feedStatus: FeedStatus)

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

    case class IndexStoreAddPodcast(podcast: Document)      // Indexer -> IndexStore
    case class IndexStoreUpdatePodcast(podcast: Document)   // Indexer -> IndexStore

    case class IndexStoreAddEpisode(episode: Document)      // Indexer -> IndexStore
    case class IndexStoreUpdateEpisode(episode: Document)   // Indexer -> IndexStore


    case class SearchRequest(query: String)                 // User -> Searcher
    case class SearchIndex(query: String)                   // Searcher -> IndexStore

    case class SearchResultsFound(query: String, results: Array[Document])  // IndexStore -> Searcher
    case class NoSearchResultsFound(query: String)                          // IndexStore -> Searcher


}
