package echo.actor.searcher

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import echo.actor.protocol.IndexProtocol.{IndexMessage, QueryIndexForPodcast, SearchResultForPodcastEpisodes}
import echo.actor.protocol.SearchProtocol.SearchQuery

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class SearcherActor (val indexRepo : ActorRef) extends Actor {

    val log = Logging(context.system, classOf[SearcherActor])

  override def receive: Receive = {

    case SearchQuery(query) => {
      log.info("Received SearchQuery('"+query+"') message")

      // TODO for now, we will pass the query 1:1 to the indexRepo; later we will have to do some query processing and additional scoring/data aggregation (show-images, etc)
      implicit val timeout = Timeout(5 seconds)

      log.info("Sending QueryIndexForPodcast('"+query+"') message")
      val future = indexRepo ? QueryIndexForPodcast(query)
      val response = Await.result(future, timeout.duration).asInstanceOf[IndexMessage]
      response match {

        case SearchResultForPodcastEpisodes(podcast, list) => {
          log.info("Received (blocking!) answer SearchResultForPodcastEpisodes('"+podcast+"',[episodes]")
          if(!list.isEmpty){
            log.info("Found Podcast('"+podcast+"') with episodes : ("+list.mkString(",")+")")
          } else {
            log.info("Podcast('"+podcast+"') does not have any Episodes saved in the Index")
          }
        }
      }

    }

  }
}
