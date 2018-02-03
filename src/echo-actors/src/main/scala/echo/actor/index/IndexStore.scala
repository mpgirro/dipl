package echo.actor.index

import akka.actor.{Actor, ActorLogging, Cancellable}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.core.exception.SearchException
import echo.core.index.{IndexCommitter, LuceneCommitter}
import echo.core.model.dto.EpisodeDTO
import echo.core.search.{IndexSearcher, LuceneSearcher}

import scala.concurrent.duration._
import scala.language.postfixOps

class IndexStore extends Actor with ActorLogging {

    private val INDEX_PATH = ConfigFactory.load().getString("echo.index")

    private val COMMIT_INTERVAL = 3000 millis // TODO read from config file

    private val indexCommitter: IndexCommitter = new LuceneCommitter(INDEX_PATH, true) // TODO do not alway re-create the index
    private val indexSearcher: IndexSearcher = new LuceneSearcher(indexCommitter.asInstanceOf[LuceneCommitter].getIndexWriter)

    import context.dispatcher
    private val commitMessager: Cancellable = context.system.scheduler.
        schedule(COMMIT_INTERVAL, COMMIT_INTERVAL) {
            self ! CommitIndex
        }

    override def receive: Receive = {

        case CommitIndex =>
            log.debug("Received CommitIndex()")
            indexCommitter.commit()

        case IndexStoreAddPodcast(podcast)  =>
            log.debug("Received IndexStoreAddPodcast({})", podcast)
            indexCommitter.add(podcast)

        case IndexStoreUpdatePodcast(podcast) =>
            log.debug("Received IndexStoreUpdatePodcast({})", podcast)
            indexCommitter.update(podcast)

        case IndexStoreAddEpisode(episode) =>
            log.debug("Received IndexStoreAddEpisode({})", episode)
            indexCommitter.add(episode)

        case IndexStoreUpdateEpisode(episode) =>
            log.debug("Received IndexStoreUpdateEpisode({})", episode)
            indexCommitter.update(episode)

        case IndexStoreUpdateDocWebsiteData(echoId, html) =>
            log.debug("Received IndexStoreUpdateDocWebsiteData({},_)", echoId)

            indexCommitter.commit() // ensure that the Podcast/Episode document is committed to the document (the message should already be processed at this point
            indexSearcher.refresh()
            val entry = Option(indexSearcher.findByEchoId(echoId))
            entry match {
                case Some(doc) =>
                    doc.setWebsiteData(html)
                    indexCommitter.update(doc)
                case None => log.error("Could not retrieve from index: echoId={}", echoId)
            }

        case IndexStoreUpdateDocItunesImage(echoId, itunesImage) =>
            log.debug("Received IndexStoreUpdateDocItunesImage({},{})", echoId, itunesImage)

            indexCommitter.commit() // ensure that the Podcast/Episode document is committed to the document (the message should already be processed at this point
            indexSearcher.refresh()

            val entry = Option(indexSearcher.findByEchoId(echoId))
            entry match {
                case Some(doc) =>
                    doc match {
                        case e: EpisodeDTO =>
                            doc.setItunesImage(itunesImage)
                            indexCommitter.update(doc)
                        case _ =>
                            log.error("Retrieved a Document by ID from Index that is not an EpisodeDocument, though I expected one")
                    }
                case None => log.error("Could not retrieve from index: echoId={}", echoId)
            }

        case SearchIndex(query, page, size) =>
            log.info("Received SearchIndex('{}',{},{}) message", query, page, size)

            indexSearcher.refresh()
            try {
                val results = indexSearcher.search(query, page, size)
                if(results.getTotalHits > 0){
                    sender ! IndexResultsFound(query,results)
                } else {
                    log.warning("No Podcast matching query: '"+query+"' found in the index")
                    sender ! NoIndexResultsFound(query)
                }
            } catch {
                case e: SearchException =>
                    log.error("Error trying to search the index [reason: {}]", e.getMessage)
                    sender ! NoIndexResultsFound(query) // TODO besser eine neue antwortmessage a la ErrorIndexResult und entsprechend den fehler in der UI anzeigen zu können
            }

    }
}
