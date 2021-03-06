package echo.actor.catalog.service

import javax.persistence.EntityManager

import akka.event.LoggingAdapter
import echo.actor.catalog.repository.{PodcastRepository, RepositoryFactoryBuilder}
import echo.core.domain.dto.PodcastDTO
import echo.core.mapper.{PodcastMapper, TeaserMapper}
import org.springframework.data.domain.{PageRequest, Sort}
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
@Repository
@Transactional
class PodcastCatalogService(log: LoggingAdapter,
                            rfb: RepositoryFactoryBuilder) extends CatalogService {

    private var repositoryFactory: JpaRepositoryFactory = _
    private var podcastRepository: PodcastRepository = _

    private val podcastMapper = PodcastMapper.INSTANCE
    private val teaserMapper = TeaserMapper.INSTANCE

    override def refresh(em: EntityManager): Unit = {
        repositoryFactory = rfb.createRepositoryFactory(em)
        podcastRepository = repositoryFactory.getRepository(classOf[PodcastRepository])
    }

    @Transactional
    def save(podcastDTO: PodcastDTO): Option[PodcastDTO] = {
        log.debug("Request to save Podcast : {}", podcastDTO)
        Option(podcastDTO)
          .map(p => podcastMapper.toEntity(p))
          .map(p => podcastRepository.save(p))
          .map(p => podcastMapper.toImmutable(p))
    }

    @Transactional
    def findOne(dbId: Long): Option[PodcastDTO] = {
        log.debug("Request to get Podcast (ID) : {}", dbId)
        Option(dbId)
          .map(id => podcastRepository.findOne(id))
          .map(p => podcastMapper.toImmutable(p))
    }

    @Transactional
    def findOneByExo(podcastExo: String): Option[PodcastDTO] = {
        log.debug("Request to get Podcast (EXO) : {}", podcastExo)
        Option(podcastExo)
          .map(exo => podcastRepository.findOneByExo(exo))
          .map(p => podcastMapper.toImmutable(p))
    }

    @Transactional
    def findOneByFeed(feedExo: String): Option[PodcastDTO] = {
        log.debug("Request to get Podcast by feed (EXO) : {}", feedExo)
        Option(feedExo)
          .map(exo => podcastRepository.findOneByFeed(exo))
          .map(p => podcastMapper.toImmutable(p))
    }

    @Transactional
    def findAll(page: Int, size: Int): List[PodcastDTO] = {
        log.debug("Request to get all Podcasts by page : {} and size : {}", page, size)
        val sort = new Sort(new Sort.Order(Direction.ASC, "title"))
        val pageable = new PageRequest(page, size, sort)
        podcastRepository.findAll(pageable)
            .asScala
            .map(p => podcastMapper.toImmutable(p))
            .toList
    }

    @Transactional
    def findAllAsTeaser(): List[PodcastDTO] = {
        log.debug("Request to get all Podcasts as teaser")
        podcastRepository.findAll()
            .asScala
            .map(p => teaserMapper.asTeaser(p))
            .toList
    }

    @Transactional
    def findAllRegistrationComplete(page: Int, size: Int): List[PodcastDTO] = {
        log.debug("Request to get all Podcasts where registration is complete by page : {} and size : {}", page, size)
        val sort = new Sort(new Sort.Order(Direction.ASC, "title"))
        val pageable = new PageRequest(page, size, sort)
        podcastRepository.findByRegistrationCompleteTrue(pageable)
            .asScala
            .map(p => podcastMapper.toImmutable(p))
            .toList
    }

    @Transactional
    def findAllRegistrationCompleteAsTeaser(page: Int, size: Int): List[PodcastDTO] = {
        log.debug("Request to get all Podcasts as teaser where registration is complete by page : {} and size : {}", page, size)
        val sort = new Sort(new Sort.Order(Direction.ASC, "title"))
        val pageable = new PageRequest(page, size, sort)
        podcastRepository.findByRegistrationCompleteTrue(pageable)
            .asScala
            .map(p => teaserMapper.asTeaser(p))
            .toList
    }

    @Transactional
    def countAll(): Long = {
        log.debug("Request to count all Podcasts")
        podcastRepository.countAll()
    }

    @Transactional
    def countAllRegistrationComplete(): Long = {
        log.debug("Request to count all Podcasts where registration is complete")
        podcastRepository.countAllRegistrationCompleteTrue()
    }
}
