package echo.actor.directory.service

import javax.persistence.EntityTransaction

import akka.event.LoggingAdapter
import echo.actor.directory.repository.{EpisodeRepository, RepositoryFactoryBuilder}
import echo.core.converter.mapper.{EpisodeMapper, PodcastMapper}
import echo.core.model.dto.{EpisodeDTO, PodcastDTO}

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class EpisodeDirectoryService(protected override val log: LoggingAdapter,
                              private val repositoryFactoryBuilder: RepositoryFactoryBuilder) extends DirectoryService[EpisodeDTO] {

    private val repositoryFactory = repositoryFactoryBuilder.createFactory
    private val episodeRepository: EpisodeRepository = repositoryFactory.getRepository(classOf[EpisodeRepository])

    protected override val em = repositoryFactoryBuilder.getEntityManager

    /*
    private val emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory
    private val episodeDao: EpisodeDao =  new EpisodeDaoImpl(emf)
    */

    override def save(episodeDTO: EpisodeDTO, tx: EntityTransaction): Option[EpisodeDTO] = {
        val episode = EpisodeMapper.INSTANCE.episodeDtoToEpisode(episodeDTO)
        val result = episodeRepository.save(episode)
        Option(EpisodeMapper.INSTANCE.episodeToEpisodeDto(result))
    }

    override def findOne(id: Long, tx: EntityTransaction): Option[EpisodeDTO] = {
        val result = episodeRepository.findOne(id)
        Option(EpisodeMapper.INSTANCE.episodeToEpisodeDto(result))
    }

    override def findOneByEchoId(echoId: String, tx: EntityTransaction): Option[EpisodeDTO] = {
        val result = episodeRepository.findOneByEchoId(echoId)
        Option(EpisodeMapper.INSTANCE.episodeToEpisodeDto(result))
    }

    override def findAll(tx: EntityTransaction): List[EpisodeDTO] = {
        val episodes = episodeRepository.findAll
        val result = EpisodeMapper.INSTANCE.episodesToEpisodesDtos(episodes)
        result.asScala.toList
    }

    def findAllByPodcast(podcastDTO: PodcastDTO): List[EpisodeDTO] = {
        val tx = em.getTransaction
        tx.begin()
        try {
            val results = findAllByPodcast(podcastDTO, tx)
            tx.commit()
            return results
        } catch {
            case e: Exception => {
                log.error("Error trying to find all episodes by podcast : {}", podcastDTO)
                tx.rollback()
            }
        }
        List.empty
    }

    def findAllByPodcast(podcastDTO: PodcastDTO, tx: EntityTransaction): List[EpisodeDTO] = {
        val podcast = PodcastMapper.INSTANCE.podcastDtoToPodcast(podcastDTO)
        val episodes = episodeRepository.findAllByPodcast(podcast)
        val result = EpisodeMapper.INSTANCE.episodesToEpisodesDtos(episodes)
        result.asScala.toList
    }

}
