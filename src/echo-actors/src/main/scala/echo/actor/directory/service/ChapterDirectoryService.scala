package echo.actor.directory.service

import javax.persistence.EntityManager

import akka.event.LoggingAdapter
import echo.actor.directory.repository.{ChapterRepository, RepositoryFactoryBuilder}
import echo.core.domain.dto.ChapterDTO
import echo.core.mapper.ChapterMapper
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class ChapterDirectoryService(private val log: LoggingAdapter,
                              private val rfb: RepositoryFactoryBuilder) extends DirectoryService {

    private var repositoryFactory: JpaRepositoryFactory = _
    private var chapterRepository: ChapterRepository = _

    override def refresh(em: EntityManager): Unit = {
        repositoryFactory = rfb.createRepositoryFactory(em)
        chapterRepository = repositoryFactory.getRepository(classOf[ChapterRepository])
    }

    @Transactional
    def save(chapterDTO: ChapterDTO): Option[ChapterDTO] = {
        val chapter = ChapterMapper.INSTANCE.map(chapterDTO)
        val result = chapterRepository.save(chapter)
        Option(ChapterMapper.INSTANCE.map(result))
    }

    @Transactional
    def saveAll(episodeId: Long, chapters: java.util.List[ChapterDTO]): Unit = {
        for(c <- chapters.asScala){
            c.setEpisodeId(episodeId)
            save(c)
        }
    }

    @Transactional
    def findAllByEpisode(episodeId: String): List[ChapterDTO] = {
        chapterRepository.findAllByEpisodeEchoId(episodeId)
            .asScala
            .map(c => ChapterMapper.INSTANCE.map(c))
            .toList
    }

}
