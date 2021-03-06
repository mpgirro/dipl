package echo.actor.index

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import echo.actor.index.IndexProtocol._

/**
  * @author Maximilian Irro
  */

class IndexStorePriorityActorMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
        case SearchIndex(_,_,_,_)                         => 0
        case AddDocIndexEvent(_,_)                      => 1
        case UpdateDocImageIndexEvent(_,_)              => 2
        case UpdateDocWebsiteDataIndexEvent(_,_)        => 2
        case UpdateDocLinkIndexEvent(_,_)               => 2
        case _                                          => 3 // other messages
    })
