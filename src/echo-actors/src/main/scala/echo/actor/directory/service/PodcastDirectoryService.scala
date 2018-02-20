package echo.actor.directory.service

import javax.persistence.EntityManager

import akka.event.LoggingAdapter
import echo.actor.directory.repository.{PodcastRepository, RepositoryFactoryBuilder}
import echo.core.domain.dto.PodcastDTO
import echo.core.domain.feed.FeedStatus
import echo.core.mapper.{PodcastMapper, TeaserMapper}
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
@Repository
@Transactional
class PodcastDirectoryService(private val log: LoggingAdapter,
                              private val rfb: RepositoryFactoryBuilder) extends DirectoryService {

    private var repositoryFactory: JpaRepositoryFactory = _
    private var podcastRepository: PodcastRepository = _

    override def refresh(em: EntityManager): Unit = {
        repositoryFactory = rfb.createRepositoryFactory(em)
        podcastRepository = repositoryFactory.getRepository(classOf[PodcastRepository])
    }

    @Transactional
    def save(podcastDTO: PodcastDTO): Option[PodcastDTO] = {
        val podcast = PodcastMapper.INSTANCE.map(podcastDTO)
        val result = podcastRepository.save(podcast)
        Option(PodcastMapper.INSTANCE.map(result))
    }

    @Transactional
    def findOne(id: Long): Option[PodcastDTO] = {
        val result = podcastRepository.findOne(id)
        Option(PodcastMapper.INSTANCE.map(result))
    }

    @Transactional
    def findOneByEchoId(echoId: String): Option[PodcastDTO] = {
        val result = podcastRepository.findOneByEchoId(echoId)
        Option(PodcastMapper.INSTANCE.map(result))
    }

    @Transactional
    def findAll(): List[PodcastDTO] = {
        podcastRepository.findAll
            .asScala
            .map(p => PodcastMapper.INSTANCE.map(p))
            .toList
    }

    @Transactional
    def findAllWhereFeedStatusIsNot(status: FeedStatus): List[PodcastDTO] = {
        podcastRepository.findAllWhereFeedStatusIsNot(status)
            .asScala
            .map(p => PodcastMapper.INSTANCE.map(p))
            .toList
    }

    @Transactional
    def findAllRegistrationComplete(): List[PodcastDTO] = {
        podcastRepository.findByRegistrationCompleteTrue()
            .asScala
            .map(p => PodcastMapper.INSTANCE.map(p))
            .toList
    }

    @Transactional
    def findAllRegistrationCompleteAsTeaser(): List[PodcastDTO] = {
        podcastRepository.findByRegistrationCompleteTrue()
            .asScala
            .map(p => TeaserMapper.INSTANCE.asTeaser(p))
            .toList
    }

}
