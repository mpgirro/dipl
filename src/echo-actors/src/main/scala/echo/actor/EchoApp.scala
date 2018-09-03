package echo.actor

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.language.postfixOps
;

object EchoApp {

    def main(args: Array[String]): Unit = {
        val config = ConfigFactory.load
        val system = ActorSystem("echo", config)
        system.actorOf(Props(new NodeMaster), NodeMaster.name)
    }

}

