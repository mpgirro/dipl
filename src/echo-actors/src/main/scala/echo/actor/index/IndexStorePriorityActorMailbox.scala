package echo.actor.index

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import echo.actor.ActorProtocol._

/**
  * @author Maximilian Irro
  */

class IndexStorePriorityActorMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
        case SearchIndex(_,_,_) => 0
        case IndexStoreAddPodcast(_) => 1
        case IndexStoreAddEpisode(_) => 1
        case IndexStoreUpdatePodcast(_) => 2
        case IndexStoreUpdateEpisode(_) => 2
        // other messages
        case _ => 3
    })
