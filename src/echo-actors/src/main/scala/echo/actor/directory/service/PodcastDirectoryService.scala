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

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class PodcastDirectoryService(private val repositoryFactoryBuilder: RepositoryFactoryBuilder) extends DirectoryService[PodcastDTO] {

    private val repositoryFactory = repositoryFactoryBuilder.createFactory
    private val podcastRepository: PodcastRepository = repositoryFactory.getRepository(classOf[PodcastRepository])

    private val em = repositoryFactoryBuilder.getEntityManager
    private val emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory
    private val podcastDao: PodcastDao = new PodcastDaoImpl(emf)

    @Transactional
    override def save(podcastDTO: PodcastDTO): PodcastDTO = {

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
    override def findOne(id: Long): Option[PodcastDTO] = {
        val result = podcastRepository.findOne(id)
        Option(PodcastMapper.INSTANCE.podcastToPodcastDto(result))
    }

    @Transactional
    override def findOneByEchoId(echoId: String): Option[PodcastDTO] = {
        val result = podcastRepository.findOneByEchoId(echoId)
        Option(PodcastMapper.INSTANCE.podcastToPodcastDto(result))
    }

    @Transactional
    override def findAll: List[PodcastDTO] = {
        val startTime = System.currentTimeMillis

        val podcasts = podcastRepository.findAll
        val result = PodcastMapper.INSTANCE.podcastsToPodcastDtos(podcasts)

        val stopTime = System.currentTimeMillis
        val elapsedTime = stopTime - startTime
        println(s"PodcastDirectoryService.findAll took ${elapsedTime} ms, found ${result.size()} entries")

        result.asScala.toList
    }

    @Transactional
    def findAllWhereFeedStatusIsNot(status: FeedStatus): List[PodcastDTO] = {

        val startTime = System.currentTimeMillis

        val podcasts = podcastRepository.findAllWhereFeedStatusIsNot(status)
        val result = PodcastMapper.INSTANCE.podcastsToPodcastDtos(podcasts)

        val stopTime = System.currentTimeMillis
        val elapsedTime = stopTime - startTime
        println(s"PodcastDirectoryService.findAllWhereFeedStatusIsNot($status) took ${elapsedTime} ms, found ${result.size()} entries")

        result.asScala.toList
    }

}
