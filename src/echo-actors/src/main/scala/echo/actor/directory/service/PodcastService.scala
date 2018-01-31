package echo.actor.directory.service

import javax.persistence.EntityManagerFactory
import javax.transaction.Transactional

import echo.actor.directory.orm.PodcastDao
import echo.actor.directory.orm.impl.PodcastDaoImpl
import echo.actor.directory.repository.{PodcastRepository, RepositoryFactoryBuilder}
import echo.core.converter.mapper.PodcastMapper
import echo.core.model.dto.PodcastDTO
import echo.core.model.feed.FeedStatus
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
  * @author Maximilian Irro
  */
class PodcastService(private val repositoryFactoryBuilder: RepositoryFactoryBuilder) {

    private val repositoryFactory = repositoryFactoryBuilder.createFactory
    private val podcastRepository: PodcastRepository = repositoryFactory.getRepository(classOf[PodcastRepository])

    private val em = repositoryFactoryBuilder.getEntityManager
    private val emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory
    private val podcastDao: PodcastDao = new PodcastDaoImpl(emf)

    @Transactional
    def save(podcastDTO: PodcastDTO): PodcastDTO = {

        //val em = repositoryFactoryBuilder.getEntityManager
        //val emf = repositoryFactoryBuilder.getEntityManagerFactory

        /*
        val tx = em.getTransaction
        podcastRepository.save(podcast)
        tx.commit
        */

        /*
        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
            val podcast = PodcastMapper.INSTANCE.podcastDtoToPodcast(podcastDTO)
            val result = podcastRepository.save(podcast)
            PodcastMapper.INSTANCE.podcastToPodcastDto(result)
        } finally {
            // Make sure to unbind when done with the repository instance
            TransactionSynchronizationManager.unbindResource(emf)
        }
        */


        val podcast = PodcastMapper.INSTANCE.podcastDtoToPodcast(podcastDTO)
        val result = podcastRepository.save(podcast)
        PodcastMapper.INSTANCE.podcastToPodcastDto(result)

    }

    @Transactional
    def findOne(id: Long): PodcastDTO = {
        val result = podcastRepository.findOne(id)
        PodcastMapper.INSTANCE.podcastToPodcastDto(result)
    }

    @Transactional
    def findOneByEchoId(echoId: String): PodcastDTO = {
        val result = podcastRepository.findOneByEchoId(echoId)
        PodcastMapper.INSTANCE.podcastToPodcastDto(result)
    }

    @Transactional
    def findAll: java.util.List[PodcastDTO] = {
        val result = podcastRepository.findAll
        PodcastMapper.INSTANCE.podcastsToPodcastDtos(result)
    }

    @Transactional
    def findAllWhereFeedStatusIsNot(status: FeedStatus): java.util.List[PodcastDTO] = {
        val result = podcastRepository.findAllWhereFeedStatusIsNot(status)
        PodcastMapper.INSTANCE.podcastsToPodcastDtos(result)
    }

}
