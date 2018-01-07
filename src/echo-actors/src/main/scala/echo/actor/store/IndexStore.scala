package echo.actor.store

import akka.actor.{Actor, ActorLogging}
import akka.event.Logging
import echo.actor.protocol.IndexProtocol._

import scala.collection.mutable.ListBuffer

class IndexStore extends Actor with ActorLogging {

//    val log = Logging(context.system, classOf[IndexStore])

    val index = scala.collection.mutable.HashMap.empty[String, ListBuffer[String]]

    override def receive: Receive = {

        case AddPodcastToIndex(podcast) => {
            log.info("Received AddPodcastToIndex('"+podcast+"') message")
            if(!index.contains(podcast)){
                log.info("Adding Podcast('"+podcast+"') to index")
                index += (podcast -> new ListBuffer[String]())
            } else {
                log.warning("Podcast('"+podcast+"') already in index")
            }
        }

        case AddEpisodeToIndex(podcast, episode) => {
            log.info("Received AddEpisodeToIndex('"+podcast+"','"+episode+"') message")
            if(index.contains(podcast)){
                log.info("Adding Episode('"+episode+"') to Podcast('"+podcast+"') in index")
                index(podcast) += episode
            } else {
                log.warning("Podcast('"+podcast+"') not in index! Can not add EpisodeEpisode('"+episode+"')")
            }
        }

        case QueryIndexForPodcast(podcast) => {
            log.info("Received QueryIndexForPodcast('"+podcast+"') message")
            if(index.contains(podcast)){
                sender ! SearchResultForPodcastEpisodes(podcast,index(podcast))
            } else {
                log.warning("No Podcast matching query: '"+podcast+"' found in the index")
            }
        }

    }
}
