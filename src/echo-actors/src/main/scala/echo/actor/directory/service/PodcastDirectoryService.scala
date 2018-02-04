package echo.actor.directory.service

import javax.persistence.{EntityManager, EntityManagerFactory}

import akka.event.LoggingAdapter
import echo.actor.directory.repository.{PodcastRepository, RepositoryFactoryBuilder}
import echo.core.converter.mapper.PodcastMapper
import echo.core.model.dto.PodcastDTO
import echo.core.model.feed.FeedStatus
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
@Transactional
class PodcastDirectoryService(private val log: LoggingAdapter,
                              private val rfb: RepositoryFactoryBuilder) {

    private val em: EntityManager = rfb.getEntityManager
    private def emf: EntityManagerFactory = rfb.getEntityManagerFactory

    private val repositoryFactory = rfb.createFactory(em)
    private val podcastRepository: PodcastRepository = repositoryFactory.getRepository(classOf[PodcastRepository])

    // private val podcastDao: PodcastDao = new PodcastDaoImpl(emf)

    @Transactional
    def save(podcastDTO: PodcastDTO): Option[PodcastDTO] = {
        val podcast = PodcastMapper.INSTANCE.podcastDtoToPodcast(podcastDTO)
        val result = podcastRepository.save(podcast)
        Option(PodcastMapper.INSTANCE.podcastToPodcastDto(result))
    }

    @Transactional(readOnly = true)
    def findOne(id: Long): Option[PodcastDTO] = {
        val result = podcastRepository.findOne(id)
        Option(PodcastMapper.INSTANCE.podcastToPodcastDto(result))
    }

    @Transactional(readOnly = true)
    def findOneByEchoId(echoId: String): Option[PodcastDTO] = {
        val result = podcastRepository.findOneByEchoId(echoId)
        Option(PodcastMapper.INSTANCE.podcastToPodcastDto(result))
    }

    @Transactional(readOnly = true)
    def findAll(): List[PodcastDTO] = {
        val podcasts = podcastRepository.findAll
        val result = PodcastMapper.INSTANCE.podcastsToPodcastDtos(podcasts)
        result.asScala.toList
    }

    @Transactional(readOnly = true)
    def findAllWhereFeedStatusIsNot(status: FeedStatus): List[PodcastDTO] = {

        val startTime = System.currentTimeMillis

        val podcasts = podcastRepository.findAllWhereFeedStatusIsNot(status)
        val result = PodcastMapper.INSTANCE.podcastsToPodcastDtos(podcasts)

        val stopTime = System.currentTimeMillis
        val elapsedTime = stopTime - startTime
        println(s"PodcastDirectoryService.findAllWhereFeedStatusIsNot($status) took $elapsedTime ms, found ${result.size()} entries")

        result.asScala.toList
    }

}
