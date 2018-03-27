package echo.actor.directory.service

import javax.persistence.EntityManager

import akka.event.LoggingAdapter
import echo.actor.directory.repository.{EpisodeRepository, RepositoryFactoryBuilder}
import echo.core.domain.dto.{EpisodeDTO, PodcastDTO}
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
class EpisodeDirectoryService (log: LoggingAdapter,
                               rfb: RepositoryFactoryBuilder) extends DirectoryService {

    private var repositoryFactory: JpaRepositoryFactory = _
    private var episodeRepository: EpisodeRepository = _

    private val podcastMapper = PodcastMapper.INSTANCE
    private val episodeMapper = EpisodeMapper.INSTANCE
    private val teaserMapper = TeaserMapper.INSTANCE

    override def refresh(em: EntityManager): Unit = {
        repositoryFactory = rfb.createRepositoryFactory(em)
        episodeRepository = repositoryFactory.getRepository(classOf[EpisodeRepository])
    }

    @Transactional
    def save(episodeDTO: EpisodeDTO): Option[EpisodeDTO] = {
        log.debug("Request to save Episode : {}", episodeDTO)
        val episode = episodeMapper.toEntity(episodeDTO)
        val result = episodeRepository.save(episode)
        Option(episodeMapper.toImmutable(result))
    }

    @Transactional(readOnly = true)
    def findOne(id: Long): Option[EpisodeDTO] = {
        log.debug("Request to get Episode (ID) : {}", id)
        val result = episodeRepository.findOne(id)
        Option(episodeMapper.toImmutable(result))
    }

    @Transactional(readOnly = true)
    def findOneByEchoId(exo: String): Option[EpisodeDTO] = {
        log.debug("Request to get Episode (EXO) : {}", exo)
        val result = episodeRepository.findOneByEchoId(exo)
        Option(episodeMapper.toImmutable(result))
    }

    @Transactional(readOnly = true)
    def findAll(): List[EpisodeDTO] = {
        log.debug("Request to get all Episodes")
        episodeRepository.findAll
            .asScala
            .map(e => episodeMapper.toImmutable(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findAllByPodcast(podcastDTO: PodcastDTO): List[EpisodeDTO] = {
        log.debug("Request to get all Episodes by Podcast : {}", podcastDTO)
        val podcast = podcastMapper.toEntity(podcastDTO)
        episodeRepository.findAllByPodcast(podcast)
            .asScala
            .map(e => episodeMapper.toImmutable(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findAllByPodcast(podcastExo: String): List[EpisodeDTO] = {
        log.debug("Request to get all Episodes by Podcast (EXO) : {}", podcastExo)
        episodeRepository.findAllByPodcastEchoId(podcastExo)
            .asScala
            .map(e => episodeMapper.toImmutable(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findAllByPodcastAsTeaser(podcastExo: String): List[EpisodeDTO] = {
        log.debug("Request to get all Episodes by Podcast (EXO) as teaser : {}", podcastExo)
        episodeRepository.findAllByPodcastEchoId(podcastExo)
            .asScala
            .map(e => teaserMapper.asTeaser(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findAllByPodcastAndGuid(podcastExo: String, guid: String): List[EpisodeDTO] = {
        log.debug("Request to get all Episodes by Podcast (EXO) : {} and GUID : {}", podcastExo, guid)
        episodeRepository.findAllByPodcastAndGuid(podcastExo, guid)
            .asScala
            .map(e => teaserMapper.asTeaser(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findOneByEnclosure(enclosureUrl: String, enclosureLength: Long, enclosureType: String): Option[EpisodeDTO] = {
        log.debug("Request to get Episode by enclosureUrl : '{}' and enclosureLength : {} and enclosureType : {}", enclosureUrl, enclosureLength, enclosureType)
        val result = episodeRepository.findOneByEnlosure(enclosureUrl, enclosureLength, enclosureType)
        Option(episodeMapper.toImmutable(result))
    }

    @Transactional(readOnly = true)
    def countAll(): Long = {
        log.debug("Request to count all Episodes")
        episodeRepository.countAll()
    }

}
