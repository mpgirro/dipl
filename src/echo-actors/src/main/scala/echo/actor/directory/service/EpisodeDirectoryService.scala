package echo.actor.directory.service

import javax.persistence.{EntityManager, EntityManagerFactory}

import akka.event.LoggingAdapter
import echo.actor.directory.repository.{EpisodeRepository, RepositoryFactoryBuilder}
import echo.core.converter.mapper.{EpisodeMapper, PodcastMapper}
import echo.core.model.dto.{EpisodeDTO, PodcastDTO}
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
@Transactional
class EpisodeDirectoryService(private val log: LoggingAdapter,
                              private val rfb: RepositoryFactoryBuilder) {

    private val em: EntityManager = rfb.getEntityManager
    private def emf: EntityManagerFactory = rfb.getEntityManagerFactory

    private val repositoryFactory = rfb.createRepositoryFactory(em)
    private val episodeRepository: EpisodeRepository = repositoryFactory.getRepository(classOf[EpisodeRepository])

    // private val episodeDao: EpisodeDao =  new EpisodeDaoImpl(emf)

    @Transactional
    def save(episodeDTO: EpisodeDTO): Option[EpisodeDTO] = {
        val episode = EpisodeMapper.INSTANCE.episodeDtoToEpisode(episodeDTO)
        val result = episodeRepository.save(episode)
        Option(EpisodeMapper.INSTANCE.episodeToEpisodeDto(result))
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
    def findAll(): List[EpisodeDTO] = {
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
