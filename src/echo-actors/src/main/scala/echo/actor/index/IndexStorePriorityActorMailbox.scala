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
        case SearchIndex(_,_,_)                         => 0
        case IndexStoreAddDoc(_)                        => 1
        case IndexStoreUpdateDocImage(_,_)        => 2
        case IndexStoreUpdateDocWebsiteData(_,_)        => 2
        case IndexStoreUpdateDocLink(_,_)               => 2
        case _                                          => 3 // other messages
    })
