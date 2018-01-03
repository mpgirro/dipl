package alokka.actor.protocol

import scala.collection.mutable.ListBuffer

object IndexProtocol {

  trait IndexMessage

  case class ProcessPodcastFeedData(feedData : String) extends IndexMessage
  case class ProcessEpisodeFeedData(feedRef : String, episodeData : String) extends IndexMessage

  case class AddPodcastToIndex(podcast : String) extends IndexMessage
  case class AddEpisodeToIndex(podcast : String, episode : String) extends IndexMessage

  case class QueryIndexForPodcast(query : String) extends IndexMessage
  case class SearchResultForPodcastEpisodes(query : String, answer : ListBuffer[String]) extends IndexMessage
}
