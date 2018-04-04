package echo.actor

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
;

object EchoApp {

    //private val log = Logger(classOf[EchoApp])

    def main(args: Array[String]): Unit = {

        val config = ConfigFactory.load
        //println("Configuration : " + config.getObject("echo"))

        val nodeName: String = Option(config.getString("echo.node-name")).getOrElse("unnamed-node")

        println("Starting node : " + nodeName)

        val system = ActorSystem("echo", config)

        system.actorOf(Props(new NodeMaster), nodeName)

        //Await.ready(system.whenTerminated, Duration.Inf)

        /*
        system.whenTerminated onComplete {
            case _ => println("EchoSystem terminated")
        }
        */

    }

}

