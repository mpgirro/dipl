package echo.actor.index

import akka.actor.{Actor, ActorLogging}
import com.typesafe.config.ConfigFactory
import echo.actor.protocol.ActorMessages._
import echo.core.exception.SearchException
import echo.core.index.{IndexCommitter, LuceneCommitter}
import echo.core.model.dto.EpisodeDTO
import echo.core.search.{IndexSearcher, LuceneSearcher}

class IndexStore extends Actor with ActorLogging {

    private val INDEX_PATH = ConfigFactory.load().getString("echo.index")

    private val indexCommitter: IndexCommitter = new LuceneCommitter(INDEX_PATH, true) // TODO do not alway re-create the index
    private val indexSearcher: IndexSearcher = new LuceneSearcher(indexCommitter.asInstanceOf[LuceneCommitter].getIndexWriter)

    override def receive: Receive = {

        case IndexStoreAddPodcast(podcast)  => {
            log.debug("IndexStoreAddPodcast({})", podcast)
            indexCommitter.add(podcast)
            indexCommitter.commit() // TODO I should do this every once in a while via an message, not every time
        }

        case IndexStoreUpdatePodcast(podcast) => {
            // TODO
            log.debug("IndexStoreUpdatePodcast({})", podcast)
            indexCommitter.update(podcast)
            indexCommitter.commit() // TODO I should do this every once in a while via an message, not every time
        }

        case IndexStoreAddEpisode(episode) => {
            log.debug("IndexStoreAddEpisode({})", episode)
            indexCommitter.add(episode)
            indexCommitter.commit() // TODO I should do this every once in a while via an message, not every time
        }

        case IndexStoreUpdateEpisode(episode) => {
            // TODO
            log.debug("IndexStoreUpdateEpisode({})", episode)
            indexCommitter.update(episode)
            indexCommitter.commit() // TODO I should do this every once in a while via an message, not every time
        }

        case IndexSoreUpdateDocumentWebsiteData(echoId, html) => {
            log.debug("Received IndexSoreUpdateDocumentWebsiteData({},_)", echoId)

            indexCommitter.commit() // ensure that the Podcast/Episode document is committed to the document (the message should already be processed at this point
            indexSearcher.refresh()
            val entry = Option(indexSearcher.findByEchoId(echoId))
            entry match {
                case Some(doc) => {
                    doc.setWebsiteData(html)
                    indexCommitter.update(doc)
                    indexCommitter.commit() // TODO I should do this every once in a while via an message, not every time
                }
                case None => log.error("Could not retrieve from index: echoId={}", echoId)
            }
        }

        case IndexStoreUpdateEpisodeAddItunesImage(echoId, itunesImage) => {
            log.debug("Received IndexStoreUpdateEpisodeAddItunesImage({},{})", echoId, itunesImage)

            indexCommitter.commit() // ensure that the Podcast/Episode document is committed to the document (the message should already be processed at this point
            indexSearcher.refresh()

            val entry = Option(indexSearcher.findByEchoId(echoId))
            entry match {
                case Some(doc) => {
                    if(doc.isInstanceOf[EpisodeDTO]){
                        doc.asInstanceOf[EpisodeDTO].setItunesImage(itunesImage)
                        indexCommitter.update(doc)
                        indexCommitter.commit() // TODO I should do this every once in a while via an message, not every time
                    } else {
                        log.error("Retrieved a Document by ID from Index that is not an EpisodeDocument, though I expected one")
                    }
                }
                case None => log.error("Could not retrieve from index: echoId={}", echoId)
            }
        }

        case SearchIndex(query, page, size) => {
            log.info("Received SearchIndex('{}',{},{}) message", query, page, size)

            indexSearcher.refresh()
            try {
                val results = indexSearcher.search(query, page, size) // TODO get page and size as arguments from message!
                if(results.getTotalHits > 0){
                    sender ! IndexResultsFound(query,results) // TODO results ist jetzt ein ResultWrapperDTO
                } else {
                    log.warning("No Podcast matching query: '"+query+"' found in the index")
                    sender ! NoIndexResultsFound(query)
                }
            } catch {
                case e: SearchException => {
                    log.error("Error trying to search the index [reason: {}]", e.getMessage)
                    sender ! NoIndexResultsFound(query) // TODO besser eine neue antwortmessage a la ErrorIndexResult und entsprechend den fehler in der UI anzeigen zu können
                }
            }



        }

    }
}
