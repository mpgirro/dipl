package echo.actor.directory.service

import javax.persistence.EntityManagerFactory
import javax.transaction.Transactional

import echo.actor.directory.orm.EpisodeDao
import echo.actor.directory.orm.impl.EpisodeDaoImpl
import echo.actor.directory.repository.{EpisodeRepository, RepositoryFactoryBuilder}
import echo.core.converter.mapper.{EpisodeMapper, PodcastMapper}
import echo.core.model.domain.Podcast
import echo.core.model.dto.{EpisodeDTO, PodcastDTO}
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.transaction.support.TransactionSynchronizationManager

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class EpisodeService(private val repositoryFactoryBuilder: RepositoryFactoryBuilder) {

    private val repositoryFactory = repositoryFactoryBuilder.createFactory
    private val episodeRepository: EpisodeRepository = repositoryFactory.getRepository(classOf[EpisodeRepository])

    private val em = repositoryFactoryBuilder.getEntityManager
    private val emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory
    private val episodeDao: EpisodeDao =  new EpisodeDaoImpl(emf)

    @Transactional
    def save(episodeDTO: EpisodeDTO): EpisodeDTO = {
        //val em = repositoryFactoryBuilder.getEntityManager
        //val emf = repositoryFactoryBuilder.getEntityManagerFactory

        /*
        val tx = em.getTransaction
        episodeRepository.save(episode)
        tx.commit
        */

        /*
        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try
            episodeRepository.save(episode) // Done in a transaction using 1 EntityManger
        finally {
            // Make sure to unbind when done with the repository instance
            TransactionSynchronizationManager.unbindResource(emf)
        }
        */

        /*
        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
        try {
            val episode = EpisodeMapper.INSTANCE.episodeDtoToEpisode(episodeDTO)
            val result = episodeRepository.save(episode) // Done in a transaction using 1 EntityManger
            EpisodeMapper.INSTANCE.episodeToEpisodeDto(result)
        } finally {
            // Make sure to unbind when done with the repository instance
            TransactionSynchronizationManager.unbindResource(emf)
        }
        */

        val episode = EpisodeMapper.INSTANCE.episodeDtoToEpisode(episodeDTO)
        val result = episodeRepository.save(episode)
        EpisodeMapper.INSTANCE.episodeToEpisodeDto(result)

    }

    @Transactional
    def findOne(id: Long): Option[EpisodeDTO] = {
        val result = episodeRepository.findOne(id)
        Option(EpisodeMapper.INSTANCE.episodeToEpisodeDto(result))
    }

    @Transactional
    def findOneByEchoId(echoId: String): Option[EpisodeDTO] = {
        val result = episodeRepository.findOneByEchoId(echoId)
        Option(EpisodeMapper.INSTANCE.episodeToEpisodeDto(result))
    }

    @Transactional
    def findAll: List[EpisodeDTO] = {
        val episodes = episodeRepository.findAll
        val result = EpisodeMapper.INSTANCE.episodesToEpisodesDtos(episodes)
        result.asScala.toList
    }

    @Transactional
    def findAllByPodcast(podcastDTO: PodcastDTO): List[EpisodeDTO] = {
        val podcast = PodcastMapper.INSTANCE.podcastDtoToPodcast(podcastDTO)
        val episodes = episodeRepository.findAllByPodcast(podcast)
        val result = EpisodeMapper.INSTANCE.episodesToEpisodesDtos(episodes)
        result.asScala.toList
    }

}
