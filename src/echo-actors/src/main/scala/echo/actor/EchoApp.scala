package echo.actor

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
;

object EchoApp {

    def main(args: Array[String]): Unit = {

        implicit val system = ActorSystem("EchoSystem", ConfigFactory.load)

        system.actorOf(Props(new MasterSupervisor), "master")

        Await.ready(system.whenTerminated, Duration.Inf)
    }

}

