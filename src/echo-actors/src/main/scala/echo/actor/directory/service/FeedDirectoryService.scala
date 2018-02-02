package echo.actor.directory.service

import javax.persistence.EntityManagerFactory
import javax.transaction.Transactional

import echo.actor.directory.orm.FeedDao
import echo.actor.directory.orm.impl.FeedDaoImpl
import echo.actor.directory.repository.{FeedRepository, RepositoryFactoryBuilder}
import echo.core.converter.mapper.FeedMapper
import echo.core.model.dto.FeedDTO
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.transaction.support.TransactionSynchronizationManager

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class FeedDirectoryService(private val repositoryFactoryBuilder: RepositoryFactoryBuilder) extends DirectoryService[FeedDTO] {

    private val repositoryFactory = repositoryFactoryBuilder.createFactory
    private val feedRepository: FeedRepository = repositoryFactory.getRepository(classOf[FeedRepository])

    private val em = repositoryFactoryBuilder.getEntityManager
    private val emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory
    private val feedDao: FeedDao =  new FeedDaoImpl(emf)

    @Transactional
    override def save(feedDTO: FeedDTO): FeedDTO = {
        //val em = repositoryFactoryBuilder.getEntityManager
        //val emf = repositoryFactoryBuilder.getEntityManagerFactory

        /*
        val tx = em.getTransaction
        feedRepository.save(feed)
        tx.commit
        */

        /*
        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
            val feed = FeedMapper.INSTANCE.feedDtoToFeed(feedDTO)
            val result = feedRepository.save(feed)
            FeedMapper.INSTANCE.feedToFeedDto(result)
        } finally {
            // Make sure to unbind when done with the repository instance
            TransactionSynchronizationManager.unbindResource(emf)
        }
        */


        val feed = FeedMapper.INSTANCE.feedDtoToFeed(feedDTO)
        val result = feedRepository.save(feed)
        FeedMapper.INSTANCE.feedToFeedDto(result)

    }

    @Transactional
    override def findOne(id: Long): Option[FeedDTO] = {
        val result = feedRepository.findOne(id)
        Option(FeedMapper.INSTANCE.feedToFeedDto(result))
    }

    @Transactional
    override def findOneByEchoId(echoId: String): Option[FeedDTO] = {
        val result = feedRepository.findOneByEchoId(echoId)
        Option(FeedMapper.INSTANCE.feedToFeedDto(result))
    }

    @Transactional
    override def findAll: List[FeedDTO] = {
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
