package echo.actor.directory

import java.sql.{Connection, DriverManager}

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol.{ActorRefCrawlerActor, ActorRefIndexStoreActor}
import liquibase.database.jvm.JdbcConnection
import liquibase.{Contexts, LabelExpression, Liquibase}
import liquibase.database.{Database, DatabaseFactory}
import liquibase.resource.ClassLoaderResourceAccessor

/**
  * @author Maximilian Irro
  */
class DirectorySupervisor extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val WORKER_COUNT: Int = Option(CONFIG.getInt("echo.directory.worker-count")).getOrElse(5)

    private var currentWorkerIndex = 1

    private var crawler: ActorRef = _
    private var indexStore: ActorRef = _

    private var router: Router = {
        val routees = Vector.fill(WORKER_COUNT) {
            val directoryStore = createDirectoryStoreWorkerActor()
            context watch directoryStore
            ActorRefRoutee(directoryStore)
        }
        Router(RoundRobinRoutingLogic(), routees)
    }

    override def preStart(): Unit = {
        runLiquibaseUpdate()
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {
        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
            router.routees.foreach(r => r.send(ActorRefCrawlerActor(crawler), sender()))

        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref
            router.routees.foreach(r => r.send(ActorRefIndexStoreActor(indexStore), sender()))

        case Terminated(corpse) =>
            /* TODO at some point we want to simply restart replace the worker
            router = router.removeRoutee(corpse)
            val directoryStore = createDirectoryActor()
            context watch directoryStore
            router = router.addRoutee(directoryStore)
            */

            /*
            router = router.removeRoutee(corpse)
            if(router.routees.isEmpty) {
                log.info("No more workers available")
                context.stop(self)
            }
            log.info("We do not re-create terminated crawlers for now")
            */

            log.error(s"A ${self.path} worker died : {}", corpse.path.name)
            context.stop(self)

        case PoisonPill =>
            log.debug("Received a PosionPill -> forwarding it to all routees")
            router.routees.foreach(r => r.send(PoisonPill, sender()))

        case work =>
            log.debug("Routing work of kind : {}", work.getClass)
            router.route(work, sender())

    }

    private def createDirectoryStoreWorkerActor(): ActorRef = {
        val workerIndex = currentWorkerIndex
        val directoryStore = context.actorOf(Props(new DirectoryStoreWorker(workerIndex))
            .withDispatcher("echo.directory.dispatcher"),
            name = "worker-" + workerIndex)
        currentWorkerIndex += 1

        // forward the actor refs to the worker, but only if those references haven't died
        Option(crawler).foreach(c => directoryStore ! ActorRefCrawlerActor(c) )
        Option(indexStore).foreach(i => directoryStore ! ActorRefIndexStoreActor(i))

        directoryStore
    }

    private def runLiquibaseUpdate(): Unit = {
        val startTime = System.currentTimeMillis
        try {
            Class.forName("org.h2.Driver")
            val conn: Connection = DriverManager.getConnection(
                "jdbc:h2:mem:echo;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                "sa",
                "")

            val database: Database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn))
            //database.setDefaultSchemaName("echo")

            val liquibase: Liquibase = new Liquibase("db/liquibase/master.xml", new ClassLoaderResourceAccessor(), database)

            val isDropFirst = true // TODO set this as a parameter
            if (isDropFirst) {
                liquibase.dropAll()
            }

            if(liquibase.isSafeToRunUpdate){
                liquibase.update(new Contexts(), new LabelExpression())
            } else {
                log.warning("Liquibase reports it is NOT safe to run the update")
            }
        } catch {
            case e: Exception =>
                log.error("Error on Liquibase update: {}", e)
        } finally {
            val stopTime = System.currentTimeMillis
            val elapsedTime = stopTime - startTime
            log.info("Run Liquibase in {} ms", elapsedTime)
        }
    }
}
