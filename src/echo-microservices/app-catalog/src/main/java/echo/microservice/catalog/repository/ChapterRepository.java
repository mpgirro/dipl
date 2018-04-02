package echo.microservice.catalog.repository;

import echo.core.domain.entity.ChapterEntity;
import echo.core.domain.entity.EpisodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Maximilian Irro
 */
public interface ChapterRepository extends JpaRepository<ChapterEntity,Long> {

    List<ChapterEntity> findAllByEpisode(EpisodeEntity episode);

    @Query("SELECT DISTINCT chapter FROM ChapterEntity chapter WHERE chapter.episode.exo = :exo")
    List<ChapterEntity> findAllByEpisodeExo(@Param("exo") String exo);

}
