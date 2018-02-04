package echo.actor.directory.service

import javax.persistence.{EntityManager, EntityManagerFactory}

import akka.event.LoggingAdapter
import echo.actor.directory.repository.{FeedRepository, RepositoryFactoryBuilder}
import echo.core.converter.mapper.FeedMapper
import echo.core.model.dto.FeedDTO
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
@Transactional
class FeedDirectoryService(private val log: LoggingAdapter,
                           private val rfb: RepositoryFactoryBuilder) {

    private val em: EntityManager = rfb.getEntityManager
    private def emf: EntityManagerFactory = rfb.getEntityManagerFactory

    private val repositoryFactory = rfb.createRepositoryFactory(em)
    private val feedRepository: FeedRepository = repositoryFactory.getRepository(classOf[FeedRepository])

    // private val feedDao: FeedDao =  new FeedDaoImpl(emf)

    @Transactional
    def save(feedDTO: FeedDTO): Option[FeedDTO] = {
        val feed = FeedMapper.INSTANCE.feedDtoToFeed(feedDTO)
        val result = feedRepository.save(feed)
        Option(FeedMapper.INSTANCE.feedToFeedDto(result))
    }

    @Transactional
    def findOne(id: Long): Option[FeedDTO] = {
        val result = feedRepository.findOne(id)
        Option(FeedMapper.INSTANCE.feedToFeedDto(result))
    }

    @Transactional
    def findOneByEchoId(echoId: String): Option[FeedDTO] = {
        val result = feedRepository.findOneByEchoId(echoId)
        Option(FeedMapper.INSTANCE.feedToFeedDto(result))
    }

    @Transactional
    def findAll(): List[FeedDTO] = {
        val feeds = feedRepository.findAll
        val results = FeedMapper.INSTANCE.feedsToFeedDtos(feeds)
        results.asScala.toList
    }

    @Transactional
    def findOneByUrl(url: String): Option[FeedDTO] = {
        val result = feedRepository.findOneByUrl(url)
        Option(FeedMapper.INSTANCE.feedToFeedDto(result))
    }

}
