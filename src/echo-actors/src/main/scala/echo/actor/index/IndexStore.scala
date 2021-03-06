package echo.actor.index

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.index.IndexProtocol._
import echo.actor.index.IndexStoreSearchHandler.RefreshIndexSearcher
import echo.core.benchmark.ArchitectureType
import echo.core.benchmark.mps.{MessagesPerSecondMeter, MessagesPerSecondMonitor, MessagesPerSecondResult}
import echo.core.benchmark.rtt.RoundTripTime
import echo.core.domain.dto.{ImmutableIndexDocDTO, IndexDocDTO}
import echo.core.exception.SearchException
import echo.core.index.{IndexCommitter, IndexSearcher, LuceneCommitter, LuceneSearcher}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.compat.java8.OptionConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */
object IndexStore {
    def name(storeIndex: Int): String = "store-" + storeIndex
    def props(indexPath: String, createIndex: Boolean): Props = {
        Props(new IndexStore(indexPath, createIndex)).withDispatcher("echo.index.dispatcher")
    }
}

class IndexStore (indexPath: String,
                  createIndex: Boolean) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val COMMIT_INTERVAL: FiniteDuration = Option(CONFIG.getInt("echo.index.commit-interval")).getOrElse(3).seconds
    /*
    private val INDEX_PATH: String = Option(CONFIG.getString("echo.index.lucene-path")).getOrElse("index")
    */
    private val WORKER_COUNT: Int = Option(CONFIG.getInt("echo.index.handler-count")).getOrElse(5)
    private var handlerIndex = 0

    private val mediator = DistributedPubSub(context.system).mediator

    private val indexCommitter: IndexCommitter = new LuceneCommitter(indexPath, createIndex) // TODO do not alway re-create the index
    private val indexSearcher: IndexSearcher = new LuceneSearcher(indexCommitter.asInstanceOf[LuceneCommitter].getIndexWriter)

    private var indexChanged = false
    private val cache: mutable.Queue[IndexDocDTO] = new mutable.Queue
    private val updateWebsiteQueue: mutable.Queue[(String,String)] = new mutable.Queue
    private val updateImageQueue: mutable.Queue[(String,String)] = new mutable.Queue
    private val updateLinkQueue: mutable.Queue[(String,String)] = new mutable.Queue

    private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.index.dispatcher")

    private var benchmarkMonitor: ActorRef = _
    private var supervisor: ActorRef = _

    private val mpsMeter = new MessagesPerSecondMeter(self.path.toStringWithoutAddress)
    private val mpsMonitor = new MessagesPerSecondMonitor(ArchitectureType.ECHO_AKKA, WORKER_COUNT)

    // kickoff the committing play
    context.system.scheduler.schedule(COMMIT_INTERVAL, COMMIT_INTERVAL, self, CommitIndex)

    private var router: Router = {
        val routees = Vector.fill(WORKER_COUNT) {
            val handler = createSearchHandler()
            context watch handler
            ActorRefRoutee(handler)
        }
        Router(RoundRobinRoutingLogic(), routees)
    }

    override def postRestart(cause: Throwable): Unit = {
        log.info("{} has been restarted or resumed", self.path.name)
        cause match {
            case e: SearchException =>
                log.error("Error trying to search the index; reason: {}", e.getMessage)
            case e: Exception =>
                log.error("Unhandled Exception : {}", e.getMessage, e)
                sender ! NoIndexResultsFound("UNKNOWN", RoundTripTime.empty()) // TODO besser eine neue antwortmessage a la ErrorIndexResult und entsprechend den fehler in der UI anzeigen zu können
                //currQuery = ""
        }
        super.postRestart(cause)
    }

    override def postStop: Unit = {
        Option(indexCommitter).foreach(_.destroy())
        Option(indexSearcher).foreach(_.destroy())

        log.info("shutting down")
    }

    override def receive: Receive = {

        case msg @ ActorRefBenchmarkMonitor(ref) =>
            log.debug("Received ActorRefBenchmarkMonitor(_)")
            benchmarkMonitor = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case ActorRefSupervisor(ref) =>
            log.debug("Received ActorRefSupervisor(_)")
            supervisor = ref

        case StartMessagePerSecondMonitoring =>
            log.debug("Received StartMessagePerSecondMonitoring(_)")
            mpsMonitor.reset()
            mpsMeter.startMeasurement()
            router.routees.foreach(r => r.send(StartMessagePerSecondMonitoring, sender()))

        case StopMessagePerSecondMonitoring =>
            log.debug("Received StopMessagePerSecondMonitoring(_)")
            if (mpsMeter.isMeasuring) {
                mpsMeter.stopMeasurement()
                //benchmarkMonitor ! MessagePerSecondReport(mpsMeter.getResult)
                router.routees.foreach(r => r.send(StopMessagePerSecondMonitoring, sender()))
            }

        case ChildMpsReport(childReport) =>
            log.debug("Received ChildMpsReport({})", childReport)
            mpsMonitor.addMetric(childReport.getName, childReport.getMps)
            if (mpsMonitor.isFinished) {

                /* when we benchmark the indexing subsystem, the MPS are generated here; in the retrieval phase, the
                 * MPS are the mean of all children */
                var mps: Double = 0.0
                if (!mpsMeter.isMeasuring && mpsMeter.getResult.getMps > 0) {
                    mps = mpsMeter.getResult.getMps
                } else {
                    mps = mpsMonitor.getDataPoints.asScala.foldLeft(0.0)(_ + _)
                }

                val selfReport = MessagesPerSecondResult.of(self.path.toStringWithoutAddress, mps)
                benchmarkMonitor ! MessagePerSecondReport(selfReport)
                supervisor ! ChildMpsReport(selfReport)
            }

        case CommitIndex =>
            commitIndexIfChanged()

        case AddDocIndexEvent(doc, rtt) =>
            log.debug("Received IndexStoreAddDoc({})", doc.getExo)
            mpsMeter.tick()

            // TODO add now to rtts and send to CLI
            benchmarkMonitor ! IndexSubSystemRoundTripTimeReport(rtt.bumpRTTs())

            cache.enqueue(doc)

        case UpdateDocWebsiteDataIndexEvent(exo, html) =>
            log.debug("Received IndexStoreUpdateDocWebsiteData({},_)", exo)
            mpsMeter.tick()
            updateWebsiteQueue.enqueue((exo,html))

        // TODO this fix is not done in the Directory and only correct data gets send to the index anyway...
        case UpdateDocImageIndexEvent(exo, image) =>
            log.debug("Received IndexStoreUpdateDocImage({},{})", exo, image)
            mpsMeter.tick()
            updateImageQueue.enqueue((exo, image))

        case UpdateDocLinkIndexEvent(exo, link) =>
            log.debug("Received IndexStoreUpdateDocLink({},'{}')", exo, link)
            mpsMeter.tick()
            updateLinkQueue.enqueue((exo, link))

        case SearchIndex(query, page, size, rtt) =>
            log.debug("Received SearchIndex('{}',{},{}) message", query, page, size)
            router.route(SearchIndex(query, page, size, rtt.bumpRTTs()), sender())

    }

    private def commitIndexIfChanged(): Unit = {
        var committed = false
        //if (indexChanged) {
        if (cache.nonEmpty) {
            log.debug("Committing Index due to pending changes")

            for (doc <- cache) {
                indexCommitter.add(doc)
            }
            indexCommitter.commit()
            //indexChanged = false
            cache.clear()

            committed = true
            log.debug("Finished Index due to pending changes")
        }

        if (updateWebsiteQueue.nonEmpty) {
            log.debug("Processing pending entries in website queue")
            indexSearcher.refresh()
            processWebsiteQueue(updateWebsiteQueue)
            indexCommitter.commit()
            committed = true
            log.debug("Finished pending entries in website queue")
        }

        if (updateImageQueue.nonEmpty) {
            log.debug("Processing pending entries in image queue")
            indexSearcher.refresh()
            processImageQueue(updateImageQueue)
            indexCommitter.commit()
            committed = true
            log.debug("Finished pending entries in image queue")
        }

        if (updateLinkQueue.nonEmpty) {
            log.debug("Processing pending entries in link queue")
            indexSearcher.refresh()
            processLinkQueue(updateLinkQueue)
            indexCommitter.commit()
            committed = true
            log.debug("Finished pending entries in link queue")
        }

        if (committed) {
            router.routees.foreach(r => r.send(RefreshIndexSearcher, self))
            indexSearcher.refresh()
        }
    }

    private def processWebsiteQueue(queue: mutable.Queue[(String,String)]): Unit = {
        if (queue.nonEmpty) {
            val (exo,html) = queue.dequeue()
            val entry = indexSearcher.findByExo(exo).asScala.map(_.asInstanceOf[ImmutableIndexDocDTO])
            entry match {
                case Some(doc) => indexCommitter.update(doc.withWebsiteData(html))
                case None      => log.error("Could not retrieve from index for update website (EXO) : {}", exo)
            }

            processWebsiteQueue(queue)
        }
    }

    private def processImageQueue(queue: mutable.Queue[(String,String)]): Unit = {
        if (queue.nonEmpty) {
            val (exo,image) = queue.dequeue()
            val entry = indexSearcher.findByExo(exo).asScala.map(_.asInstanceOf[ImmutableIndexDocDTO])
            entry match {
                case Some(doc) => indexCommitter.update(doc.withImage(image))
                case None      => log.error("Could not retrieve from index for update image (EXO) : {}", exo)
            }

            processImageQueue(queue)
        }
    }

    private def processLinkQueue(queue: mutable.Queue[(String,String)]): Unit = {
        if (queue.nonEmpty) {
            val (exo,link) = queue.dequeue()
            val entry = indexSearcher.findByExo(exo).asScala.map(_.asInstanceOf[ImmutableIndexDocDTO])
            entry match {
                case Some(doc) => indexCommitter.update(doc.withLink(link))
                case None      => log.error("Could not retrieve from index for update link (EXO) : {}", exo)
            }

            processLinkQueue(queue)
        }
    }

    private def createSearchHandler(): ActorRef = {
        handlerIndex += 1
        val newIndexSearcher: IndexSearcher = new LuceneSearcher(indexCommitter.asInstanceOf[LuceneCommitter].getIndexWriter)
        val handler = context.actorOf(IndexStoreSearchHandler.props(newIndexSearcher), IndexStoreSearchHandler.name(handlerIndex))

        handler ! ActorRefSupervisor(self)

        handler
    }

}
