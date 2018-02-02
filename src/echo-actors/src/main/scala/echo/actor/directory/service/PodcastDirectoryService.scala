package echo.actor.directory.service

import javax.persistence.{EntityManager, EntityManagerFactory, EntityTransaction}

import akka.event.LoggingAdapter
import echo.actor.directory.orm.PodcastDao
import echo.actor.directory.orm.impl.PodcastDaoImpl
import echo.actor.directory.repository.{PodcastRepository, RepositoryFactoryBuilder}
import echo.core.converter.mapper.PodcastMapper
import echo.core.model.dto.PodcastDTO
import echo.core.model.feed.FeedStatus

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class PodcastDirectoryService(protected override val log: LoggingAdapter,
                              private val repositoryFactoryBuilder: RepositoryFactoryBuilder) extends DirectoryService[PodcastDTO] {

    private val repositoryFactory = repositoryFactoryBuilder.createFactory
    private val podcastRepository: PodcastRepository = repositoryFactory.getRepository(classOf[PodcastRepository])

    protected override val em: EntityManager = repositoryFactoryBuilder.getEntityManager
    private val emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory
    private val podcastDao: PodcastDao = new PodcastDaoImpl(emf)

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

    override def save(podcastDTO: PodcastDTO, tx: EntityTransaction): Option[PodcastDTO] = {
        val podcast = PodcastMapper.INSTANCE.podcastDtoToPodcast(podcastDTO)
        val result = podcastRepository.save(podcast)
        Option(PodcastMapper.INSTANCE.podcastToPodcastDto(result))
    }

    override def findOne(id: Long, tx: EntityTransaction): Option[PodcastDTO] = {
        val result = podcastRepository.findOne(id)
        Option(PodcastMapper.INSTANCE.podcastToPodcastDto(result))
    }

    override def findOneByEchoId(echoId: String, tx: EntityTransaction): Option[PodcastDTO] = {
        val result = podcastRepository.findOneByEchoId(echoId)
        Option(PodcastMapper.INSTANCE.podcastToPodcastDto(result))
    }

    override def findAll(tx: EntityTransaction): List[PodcastDTO] = {
        val podcasts = podcastRepository.findAll
        val result = PodcastMapper.INSTANCE.podcastsToPodcastDtos(podcasts)
        result.asScala.toList
    }

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
