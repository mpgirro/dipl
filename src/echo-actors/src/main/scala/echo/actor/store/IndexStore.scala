package echo.actor.store

import akka.actor.{Actor, ActorLogging}
import com.typesafe.config.ConfigFactory
import echo.actor.protocol.ActorMessages._
import echo.core.dto.document.Document
import echo.core.index.{IndexCommitter, LuceneCommitter}
import echo.core.search.{IndexSearcher, LuceneSearcher}

class IndexStore extends Actor with ActorLogging {

//    val log = Logging(context.system, classOf[IndexStore])
    val INDEX_PATH = ConfigFactory.load().getString("echo.index")

    val indexCommitter: IndexCommitter = new LuceneCommitter(INDEX_PATH, true) // TODO do not alway re-create the index
    val indexSearcher: IndexSearcher = new LuceneSearcher(indexCommitter.asInstanceOf[LuceneCommitter].getIndexWriter)

    override def receive: Receive = {

        case IndexStoreAddPodcast(podcast)  => {
            log.debug("Adding " + podcast)
            indexCommitter.add(podcast)
            indexCommitter.commit() // TODO I should do this every once in a while via an message, not every time
        }

        case IndexStoreUpdatePodcast(podcast) => {
            // TODO
            log.debug("Updating " + podcast)
            indexCommitter.update(podcast)
            indexCommitter.commit() // TODO I should do this every once in a while via an message, not every time
        }

        case IndexStoreAddEpisode(episode) => {
            log.debug("Adding " + episode)
            indexCommitter.add(episode)
            indexCommitter.commit() // TODO I should do this every once in a while via an message, not every time
        }

        case IndexStoreUpdateEpisode(episode) => {
            // TODO
            log.debug("Updating " + episode)
            indexCommitter.update(episode)
            indexCommitter.commit() // TODO I should do this every once in a while via an message, not every time
        }

        case SearchIndex(query: String) => {
            log.info("Received SearchIndex('"+query+"') message")

            indexSearcher.refresh()
            val results = indexSearcher.search(query)
            if(results.length > 0){
                sender ! IndexResultsFound(query,results)
            } else {
                log.warning("No Podcast matching query: '"+query+"' found in the index")
                sender ! NoIndexResultsFound(query)
            }

        }

    }
}
