package echo.core.async.job;

import com.google.common.base.MoreObjects;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.PodcastDTO;

/**
 * @author Maximilian Irro
 */
public class UpdatePodcastCatalogJob implements CatalogJob {

    private PodcastDTO podcast;

    public UpdatePodcastCatalogJob() {

    }

    public UpdatePodcastCatalogJob(PodcastDTO podcast) {
        this.podcast = podcast;
    }

    public PodcastDTO getPodcast() {
        return podcast;
    }

    public void setPodcast(PodcastDTO podcast) {
        this.podcast = podcast;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("UpdatePodcastCatalogJob")
            .omitNullValues()
            .add("podcast", podcast)
            .toString();
    }

}
