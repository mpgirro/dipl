package echo.actor

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import echo.actor.crawler.CrawlerActor
import echo.actor.gateway.GatewayActor
import echo.actor.indexer.IndexerActor
import echo.actor.protocol.ActorMessages._
import echo.actor.searcher.SearcherActor
import echo.actor.store.{DirectoryStore, IndexStore}
import echo.core.util.DocumentFormatter

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.{Source, StdIn}
import scala.language.postfixOps
;

object EchoApp {

    def main(args: Array[String]): Unit = {

        implicit val system = ActorSystem("EchoSystem", ConfigFactory.load)

        system.actorOf(Props(new EchoMaster), "master")

        Await.ready(system.whenTerminated, Duration.Inf)
    }

}

