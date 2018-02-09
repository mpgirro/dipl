package echo.actor.directory

import java.sql.{Connection, DriverManager}

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import echo.actor.ActorProtocol.{ActorRefCrawlerActor, ActorRefIndexStoreActor}
import liquibase.database.jvm.JdbcConnection
import liquibase.{Contexts, LabelExpression, Liquibase}
import liquibase.database.{Database, DatabaseFactory}
import liquibase.resource.ClassLoaderResourceAccessor

/**
  * @author Maximilian Irro
  */
class DirectorySupervisor extends Actor with ActorLogging {

    private val WORKER_COUNT = 5 // TODO read this from config
    private var workerIndex = 1

    private var crawler: ActorRef = _
    private var indexStore: ActorRef = _

    private var router: Router = {
        val routees = Vector.fill(WORKER_COUNT) {
            val directoryStore = createDirectoryActor()
            context watch directoryStore
            ActorRefRoutee(directoryStore)
        }
        Router(RoundRobinRoutingLogic(), routees)
    }

    override def preStart(): Unit = {
        runLiquibaseUpdate()
    }

    override def postStop: Unit = {
        log.info(s"${self.path.name} shut down")
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

        case Terminated(a) =>
            router = router.removeRoutee(a)
            val directoryStore = createDirectoryActor()
            context watch directoryStore
            router = router.addRoutee(directoryStore)

        case work =>
            router.route(work, sender())
    }

    private def createDirectoryActor(): ActorRef = {
        val directoryStore = context.actorOf(Props[DirectoryStore]
            .withDispatcher("echo.directory.dispatcher"),
            name = "directory-" + workerIndex)

        workerIndex += 1

        Option(crawler).map(c => directoryStore ! ActorRefCrawlerActor(c) )
        Option(indexStore).map(i => directoryStore ! ActorRefIndexStoreActor(i))

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
