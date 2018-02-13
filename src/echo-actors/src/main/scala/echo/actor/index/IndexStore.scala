package echo.actor.index

import akka.actor.{Actor, ActorLogging, Cancellable}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.core.exception.SearchException
import echo.core.index.{IndexCommitter, LuceneCommitter}
import echo.core.model.dto.EpisodeDTO
import echo.core.search.{IndexSearcher, LuceneSearcher}

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class IndexStore extends Actor with ActorLogging {

    private val INDEX_PATH = ConfigFactory.load().getString("echo.index.lucene-path")

    private val COMMIT_INTERVAL = 3 seconds // TODO read from config file

    private val indexCommitter: IndexCommitter = new LuceneCommitter(INDEX_PATH, true) // TODO do not alway re-create the index
    private val indexSearcher: IndexSearcher = new LuceneSearcher(indexCommitter.asInstanceOf[LuceneCommitter].getIndexWriter)

    private var indexChanged = false
    private val updateWebsiteQueue: mutable.Queue[(String,String)] = new mutable.Queue
    private val updateImageQueue: mutable.Queue[(String,String)] = new mutable.Queue
    private val updateLinkQueue: mutable.Queue[(String,String)] = new mutable.Queue

    //import context.dispatcher
    private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.index.dispatcher")

    private val commitMessager: Cancellable = context.system.scheduler.
        schedule(COMMIT_INTERVAL, COMMIT_INTERVAL) {
            self ! CommitIndex
        }

    override def postStop: Unit = {
        indexCommitter.destroy()
        indexSearcher.destroy()

        log.info(s"${self.path.name} shut down")
    }

    override def receive: Receive = {

        case CommitIndex =>
            commitIndexIfChanged()

        case IndexStoreAddPodcast(podcast)  =>
            log.debug("Received IndexStoreAddPodcast({})", podcast.getEchoId)
            indexCommitter.add(podcast)
            indexChanged = true
            log.debug("Exit IndexStoreAddPodcast({})", podcast.getEchoId)

        case IndexStoreUpdatePodcast(podcast) =>
            log.debug("Received IndexStoreUpdatePodcast({})", podcast.getEchoId)
            indexCommitter.update(podcast)
            indexChanged = true
            log.debug("Exit IndexStoreUpdatePodcast({})", podcast.getEchoId)

        case IndexStoreAddEpisode(episode) =>
            log.debug("Received IndexStoreAddEpisode({})", episode.getEchoId)
            indexCommitter.add(episode)
            indexChanged = true
            log.debug("Exit IndexStoreAddEpisode({})", episode.getEchoId)

        case IndexStoreUpdateEpisode(episode) =>
            log.debug("Received IndexStoreUpdateEpisode({})", episode.getEchoId)
            indexCommitter.update(episode)
            indexChanged = true
            log.debug("Exit IndexStoreUpdateEpisode({})", episode.getEchoId)

        case IndexStoreUpdateDocWebsiteData(echoId, html) =>
            log.debug("Received IndexStoreUpdateDocWebsiteData({},_)", echoId)
            updateWebsiteQueue.enqueue((echoId,html))
            log.debug("Exit IndexStoreUpdateDocWebsiteData({},_)", echoId)

        case IndexStoreUpdateDocItunesImage(echoId, itunesImage) =>
            log.debug("Received IndexStoreUpdateDocItunesImage({},{})", echoId, itunesImage)
            updateImageQueue.enqueue((echoId, itunesImage))
            log.debug("Exit IndexStoreUpdateDocItunesImage({},{})", echoId, itunesImage)

        case IndexStoreUpdateDocLink(echoId, link) =>
            log.debug("Received IndexStoreUpdateDocLink({},'{}')", echoId, link)
            updateLinkQueue.enqueue((echoId, link))
            log.debug("Exit IndexStoreUpdateDocLink({},'{}')", echoId, link)

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
            log.info("Exit SearchIndex('{}',{},{}) message", query, page, size)

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
        if(queue.isEmpty) {
            return
        }

        val (echoId,html) = queue.dequeue()
        val entry = Option(indexSearcher.findByEchoId(echoId))
        entry match {
            case Some(doc) =>
                doc.setWebsiteData(html)
                indexCommitter.update(doc)
            case None => log.error("Could not retrieve from index: echoId={}", echoId)
        }

        processWebsiteQueue(queue)
    }

    private def processImageQueue(queue: mutable.Queue[(String,String)]): Unit = {

        if(queue.isEmpty) return

        val (echoId,itunesImage) = queue.dequeue()
        val entry = Option(indexSearcher.findByEchoId(echoId))
        entry match {
            case Some(doc) =>
                doc.setItunesImage(itunesImage)
                indexCommitter.update(doc)
            case None => log.error("Could not retrieve from index: echoId={}", echoId)
        }

        processImageQueue(queue)
    }

    private def processLinkQueue(queue: mutable.Queue[(String,String)]): Unit = {

        if(queue.isEmpty) return

        val (echoId,link) = queue.dequeue()
        val entry = Option(indexSearcher.findByEchoId(echoId))
        entry match {
            case Some(doc) =>
                doc.setLink(link)
                indexCommitter.update(doc)
            case None => log.error("Could not retrieve from index: echoId={}", echoId)
        }

        processLinkQueue(queue)
    }

}
