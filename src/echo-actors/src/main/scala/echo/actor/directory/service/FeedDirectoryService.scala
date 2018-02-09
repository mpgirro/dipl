package echo.actor.directory.service

import javax.persistence.EntityManager

import akka.event.LoggingAdapter
import echo.actor.directory.repository.{FeedRepository, RepositoryFactoryBuilder}
import echo.core.mapper.FeedMapper
import echo.core.model.dto.FeedDTO
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
@Repository
@Transactional
class FeedDirectoryService(private val log: LoggingAdapter,
                           private val rfb: RepositoryFactoryBuilder) extends DirectoryService {

    // private val feedDao: FeedDao =  new FeedDaoImpl(emf)

    private var repositoryFactory: JpaRepositoryFactory = _
    private var feedRepository: FeedRepository = _

    override def refresh(em: EntityManager): Unit = {
        repositoryFactory = rfb.createRepositoryFactory(em)
        feedRepository = repositoryFactory.getRepository(classOf[FeedRepository])
    }

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
