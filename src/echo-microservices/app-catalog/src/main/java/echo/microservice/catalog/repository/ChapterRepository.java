package echo.microservice.catalog.repository;

import echo.core.domain.entity.Chapter;
import echo.core.domain.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Maximilian Irro
 */
public interface ChapterRepository extends JpaRepository<Chapter,Long> {

    List<Chapter> findAllByEpisode(Episode episode);

    @Query("SELECT DISTINCT chapter FROM Chapter chapter WHERE chapter.episode.echoId = :echoId")
    List<Chapter> findAllByEpisodeEchoId(@Param("echoId") String echoId);

}
