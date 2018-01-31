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

/**
  * @author Maximilian Irro
  */
class FeedService(private val repositoryFactoryBuilder: RepositoryFactoryBuilder) {

    private val repositoryFactory = repositoryFactoryBuilder.createFactory
    private val feedRepository: FeedRepository = repositoryFactory.getRepository(classOf[FeedRepository])

    private val em = repositoryFactoryBuilder.getEntityManager
    private val emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory
    private val feedDao: FeedDao =  new FeedDaoImpl(emf)

    @Transactional
    def save(feedDTO: FeedDTO): FeedDTO = {
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
    def findOne(id: Long): FeedDTO = {
        val result = feedRepository.findOne(id)
        FeedMapper.INSTANCE.feedToFeedDto(result)
    }

    @Transactional
    def findOneByUrl(url: String): FeedDTO = {
        val result = feedRepository.findOneByUrl(url)
        FeedMapper.INSTANCE.feedToFeedDto(result)
    }

}
