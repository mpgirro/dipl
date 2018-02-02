package echo.actor.directory.service

import javax.persistence.{EntityManager, EntityTransaction}

import akka.event.LoggingAdapter
import echo.actor.directory.repository.{FeedRepository, RepositoryFactoryBuilder}
import echo.core.converter.mapper.FeedMapper
import echo.core.model.dto.FeedDTO

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class FeedDirectoryService(protected override val log: LoggingAdapter,
                           private val repositoryFactoryBuilder: RepositoryFactoryBuilder) extends DirectoryService[FeedDTO] {

    private val repositoryFactory = repositoryFactoryBuilder.createFactory
    private val feedRepository: FeedRepository = repositoryFactory.getRepository(classOf[FeedRepository])

    protected override val em: EntityManager = repositoryFactoryBuilder.getEntityManager

    /*
    private val emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory
    private val feedDao: FeedDao =  new FeedDaoImpl(emf)
    */

    override def save(feedDTO: FeedDTO, tx: EntityTransaction): Option[FeedDTO] = {
        val feed = FeedMapper.INSTANCE.feedDtoToFeed(feedDTO)
        val result = feedRepository.save(feed)
        Option(FeedMapper.INSTANCE.feedToFeedDto(result))
    }

    override def findOne(id: Long, tx: EntityTransaction): Option[FeedDTO] = {
        val result = feedRepository.findOne(id)
        Option(FeedMapper.INSTANCE.feedToFeedDto(result))
    }

    override def findOneByEchoId(echoId: String, tx: EntityTransaction): Option[FeedDTO] = {
        val result = feedRepository.findOneByEchoId(echoId)
        Option(FeedMapper.INSTANCE.feedToFeedDto(result))
    }

    override def findAll(tx: EntityTransaction): List[FeedDTO] = {
        val feeds = feedRepository.findAll
        val results = FeedMapper.INSTANCE.feedsToFeedDtos(feeds)
        results.asScala.toList
    }

    def findOneByUrl(url: String): Option[FeedDTO] = {
        val tx = em.getTransaction
        tx.begin()
        try {
            val result = findOneByUrl(url, tx)
            tx.commit()
            return result
        } catch {
            case e: Exception =>
                log.error("Error trying to find all feeds")
                tx.rollback()
        }
        None
    }

    def findOneByUrl(url: String, tx: EntityTransaction): Option[FeedDTO] = {
        val result = feedRepository.findOneByUrl(url)
        Option(FeedMapper.INSTANCE.feedToFeedDto(result))
    }

}
