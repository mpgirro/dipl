package echo.microservice.cli

import com.typesafe.scalalogging.Logger
import com.softwaremill.sttp._
import scala.io.{Source, StdIn}

class CliApp {

    private val log = Logger(classOf[CliApp])

    private var shutdown = false

    val usageMap = Map(
        "propose"        -> "feed [feed [feed]]",
        "check podcast"  -> "[all|<echoId>]",
        "check feed"     -> "[all|<echoId>]",
        "count"          -> "[podcasts|episodes|feeds]",
        "search"         -> "query [query [query]]",
        "print database" -> "[podcasts|episodes|feeds]",
        "load feeds"     -> "[test|massive]",
        "load fyyd"      -> "[episodes <podcastId> <fyydId>]",
        "save feeds"     -> "<dest>",
        "crawl fyyd"     -> "count",
        "get podcast"    -> "<echoId>",
        "get episode"    -> "<echoId>"
    )

    // TODO
    private val CATALOG_URL = "http://localhost:3031/catalog"

    private implicit val backend = HttpURLConnectionBackend()

    private def repl() {
        log.info("CLI read to take commands")
        while(!shutdown){
            val input = StdIn.readLine()
            def exec(commands: Array[String]): Unit = {
                commands.toList match {
                    case "help" :: _ => help()
                    case q@("q" | "quit" | "exit") :: _ => shutdown = true

                    case "propose" :: Nil   => usage("propose")
                    case "propose" :: feeds => feeds.foreach(f => propose(f))

                    case _  => help()
                }
            }
            Option(input).foreach(i => exec(i.split(" ")))
        }

        log.info("Terminating due to user request")
    }

    private def usage(cmd: String): Unit = {
        if (usageMap.contains(cmd)) {
            val args = usageMap.get(cmd)
            println("Command parsing error")
            println("Usage: " + cmd + " " + args)
        } else {
            println("Unknown command: " + cmd)
            println("These are the available commands:")
            for ( (k,v) <- usageMap ) {
                println(k + "\t" + v)
            }
        }
    }

    private def help(): Unit = {
        println("This is an interactive REPL providing a CLI to the search engine. Functions are:\n")
        for ( (k,v) <- usageMap ) {
            println(k + "\t" + v)
        }
        println("\nFeel free to play around!\n")
    }

    private def propose(url: String): Unit = {
        sttp.post(uri"${CATALOG_URL}/feed/propose?url=${url}").send()
    }
}

object CliApp {
    def main(args:Array[String]){
        val app = new CliApp
        app.repl()
    }
}


