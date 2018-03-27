package echo.microservice.catalog.repository;

import echo.core.domain.entity.PodcastEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Maximilian Irro
 */
public interface PodcastRepository extends JpaRepository<PodcastEntity,Long> {

    PodcastEntity findOneByEchoId(String echoId);

    @Query("SELECT DISTINCT podcast FROM PodcastEntity podcast " +
            "LEFT JOIN podcast.feeds feed " +
            "WHERE feed.echoId = :feedId")
    PodcastEntity findOneByFeed(@Param("feedId") String feedId);

    List<PodcastEntity> findByRegistrationCompleteTrue(Pageable pageable);

    @Query("SELECT count(podcast) FROM PodcastEntity podcast")
    Long countAll();

    @Query("SELECT count(podcast) FROM PodcastEntity podcast WHERE podcast.registrationComplete = true")
    Long countAllRegistrationCompleteTrue();

}
