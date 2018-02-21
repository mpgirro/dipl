package echo.actor.directory.service

import javax.persistence.EntityManager

import akka.event.LoggingAdapter
import echo.actor.directory.repository.{EpisodeRepository, RepositoryFactoryBuilder}
import echo.core.domain.dto.{EpisodeDTO, PodcastDTO}
import echo.core.domain.entity.Episode
import echo.core.mapper.{EpisodeMapper, PodcastMapper, TeaserMapper}
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
@Repository
@Transactional
class EpisodeDirectoryService(private val log: LoggingAdapter,
                              private val rfb: RepositoryFactoryBuilder) extends DirectoryService {

    private var repositoryFactory: JpaRepositoryFactory = _
    private var episodeRepository: EpisodeRepository = _

    override def refresh(em: EntityManager): Unit = {
        repositoryFactory = rfb.createRepositoryFactory(em)
        episodeRepository = repositoryFactory.getRepository(classOf[EpisodeRepository])
    }

    @Transactional
    def save(episodeDTO: EpisodeDTO): Option[EpisodeDTO] = {
        val episode = EpisodeMapper.INSTANCE.map(episodeDTO)
        val result = episodeRepository.save(episode)
        Option(EpisodeMapper.INSTANCE.map(result))
    }

    @Transactional
    def findOne(id: Long): Option[EpisodeDTO] = {
        val result = episodeRepository.findOne(id)
        Option(EpisodeMapper.INSTANCE.map(result))
    }

    @Transactional
    def findOneByEchoId(echoId: String): Option[EpisodeDTO] = {
        val result = episodeRepository.findOneByEchoId(echoId)
        Option(EpisodeMapper.INSTANCE.map(result))
    }

    @Transactional
    def findAll(): List[EpisodeDTO] = {
        episodeRepository.findAll
            .asScala
            .map(e => EpisodeMapper.INSTANCE.map(e))
            .toList
    }

    @Transactional
    def findAllByPodcast(podcastDTO: PodcastDTO): List[EpisodeDTO] = {
        val podcast = PodcastMapper.INSTANCE.map(podcastDTO)
        episodeRepository.findAllByPodcast(podcast)
            .asScala
            .map(e => EpisodeMapper.INSTANCE.map(e))
            .toList
    }

    @Transactional
    def findAllByPodcastAsTeaser(echoId: String): List[EpisodeDTO] = {
        episodeRepository.findAllByPodcastEchoId(echoId)
            .asScala
            .map(e => TeaserMapper.INSTANCE.asTeaser(e))
            .toList
    }

    @Transactional
    def findOneByPodcastAndGuid(podcastId: String, guid: String): Option[EpisodeDTO] = {
        val result = episodeRepository.findOneByPodcastAndGuid(podcastId, guid)
        Option(EpisodeMapper.INSTANCE.map(result))
    }

    @Transactional
    def findOneByEnclosure(enclosureUrl: String, enclosureLength: Long, enclosureType: String): Option[EpisodeDTO] = {
        val result = episodeRepository.findOneByEnlosure(enclosureUrl, enclosureLength, enclosureType)
        Option(EpisodeMapper.INSTANCE.map(result))
    }

}
