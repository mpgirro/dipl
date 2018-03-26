package echo.actor.index

import akka.actor.{Actor, ActorLogging}
import com.typesafe.config.ConfigFactory
import echo.actor.index.IndexProtocol._
import echo.core.domain.dto.ImmutableIndexDocDTO
import echo.core.exception.SearchException
import echo.core.index.{IndexCommitter, IndexSearcher, LuceneCommitter, LuceneSearcher}

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class IndexStore (val indexPath: String, val createIndex: Boolean) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val COMMIT_INTERVAL: FiniteDuration = Option(CONFIG.getInt("echo.index.commit-interval")).getOrElse(3).seconds
    /*
    private val INDEX_PATH: String = Option(CONFIG.getString("echo.index.lucene-path")).getOrElse("index")
    */

    private val indexCommitter: IndexCommitter = new LuceneCommitter(indexPath, createIndex) // TODO do not alway re-create the index
    private val indexSearcher: IndexSearcher = new LuceneSearcher(indexCommitter.asInstanceOf[LuceneCommitter].getIndexWriter)

    private var indexChanged = false
    private val updateWebsiteQueue: mutable.Queue[(String,String)] = new mutable.Queue
    private val updateImageQueue: mutable.Queue[(String,String)] = new mutable.Queue
    private val updateLinkQueue: mutable.Queue[(String,String)] = new mutable.Queue

    private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.index.dispatcher")

    // kickoff the committing play
    context.system.scheduler.scheduleOnce(COMMIT_INTERVAL, self, CommitIndex)

    override def postStop: Unit = {
        Option(indexCommitter).foreach(_.destroy())
        Option(indexSearcher).foreach(_.destroy())

        log.info("shutting down")
    }

    override def receive: Receive = {

        case CommitIndex =>
            commitIndexIfChanged()
            context.system.scheduler.scheduleOnce(COMMIT_INTERVAL, self, CommitIndex)

        case IndexStoreAddDoc(doc) =>
            log.debug("Received IndexStoreAddDoc({})", doc.getEchoId)
            indexCommitter.add(doc)
            indexChanged = true
            log.debug("Exit IndexStoreAddDoc({})", doc.getEchoId)

        case IndexStoreUpdateDocWebsiteData(echoId, html) =>
            log.debug("Received IndexStoreUpdateDocWebsiteData({},_)", echoId)
            updateWebsiteQueue.enqueue((echoId,html))
            log.debug("Exit IndexStoreUpdateDocWebsiteData({},_)", echoId)

        // TODO this fix is not done in the Directory and only correct data gets send to the index anyway...
        case IndexStoreUpdateDocImage(echoId, image) =>
            log.debug("Received IndexStoreUpdateDocImage({},{})", echoId, image)
            updateImageQueue.enqueue((echoId, image))
            log.debug("Exit IndexStoreUpdateDocImage({},{})", echoId, image)

        case IndexStoreUpdateDocLink(echoId, link) =>
            log.debug("Received IndexStoreUpdateDocLink({},'{}')", echoId, link)
            updateLinkQueue.enqueue((echoId, link))
            log.debug("Exit IndexStoreUpdateDocLink({},'{}')", echoId, link)

        case SearchIndex(query, page, size) =>
            log.debug("Received SearchIndex('{}',{},{}) message", query, page, size)

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
            log.debug("Exit SearchIndex('{}',{},{}) message", query, page, size)

    }

    private def commitIndexIfChanged(): Unit = {
        if(indexChanged) {
            log.debug("Committing Index due to pending changes")
            indexCommitter.commit()
            indexChanged = false
            log.debug("Finished Index due to pending changes")
        }

        if(updateWebsiteQueue.nonEmpty){
            log.debug("Processing pending entries in website queue")
            indexSearcher.refresh()
            processWebsiteQueue(updateWebsiteQueue)
            indexCommitter.commit()
            log.debug("Finished pending entries in website queue")
        }

        if(updateImageQueue.nonEmpty){
            log.debug("Processing pending entries in image queue")
            indexSearcher.refresh()
            processImageQueue(updateImageQueue)
            indexCommitter.commit()
            log.debug("Finished pending entries in image queue")
        }

        if(updateLinkQueue.nonEmpty){
            log.debug("Processing pending entries in link queue")
            indexSearcher.refresh()
            processLinkQueue(updateLinkQueue)
            indexCommitter.commit()
            log.debug("Finished pending entries in link queue")
        }
    }

    private def processWebsiteQueue(queue: mutable.Queue[(String,String)]): Unit = {
        if (queue.nonEmpty) {
            val (echoId,html) = queue.dequeue()
            val entry = Option(indexSearcher.findByEchoId(echoId)).map(_.asInstanceOf[ImmutableIndexDocDTO])
            entry match {
                case Some(doc) => indexCommitter.update(doc.withWebsiteData(html))
                case None      => log.error("Could not retrieve from index for update website: echoId={}", echoId)
            }

            processWebsiteQueue(queue)
        }
    }

    private def processImageQueue(queue: mutable.Queue[(String,String)]): Unit = {
        if (queue.nonEmpty) {
            val (echoId,image) = queue.dequeue()
            val entry = Option(indexSearcher.findByEchoId(echoId)).map(_.asInstanceOf[ImmutableIndexDocDTO])
            entry match {
                case Some(doc) => indexCommitter.update(doc.withImage(image))
                case None      => log.error("Could not retrieve from index for update image: echoId={}", echoId)
            }

            processImageQueue(queue)
        }
    }

    private def processLinkQueue(queue: mutable.Queue[(String,String)]): Unit = {
        if (queue.nonEmpty) {
            val (echoId,link) = queue.dequeue()
            val entry = Option(indexSearcher.findByEchoId(echoId)).map(_.asInstanceOf[ImmutableIndexDocDTO])
            entry match {
                case Some(doc) => indexCommitter.update(doc.withLink(link))
                case None      => log.error("Could not retrieve from index for update link: echoId={}", echoId)
            }

            processLinkQueue(queue)
        }
    }

}
