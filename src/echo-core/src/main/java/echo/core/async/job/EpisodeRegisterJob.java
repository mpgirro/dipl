package echo.core.async.job;

import com.google.common.base.MoreObjects;
import echo.core.domain.dto.EpisodeDTO;

/**
 * @author Maximilian Irro
 */
public class EpisodeRegisterJob implements CatalogJob {

    private String podcastExo;
    private EpisodeDTO episode;

    public EpisodeRegisterJob() {

    }

    public EpisodeRegisterJob(String podcastExo, EpisodeDTO episode) {
        this.podcastExo = podcastExo;
        this.episode = episode;
    }

    public String getPodcastExo() {
        return podcastExo;
    }

    public void setPodcastExo(String podcastExo) {
        this.podcastExo = podcastExo;
    }

    public EpisodeDTO getEpisode() {
        return episode;
    }

    public void setEpisode(EpisodeDTO episode) {
        this.episode = episode;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("EpisodeRegisterJob")
            .omitNullValues()
            .add("podcastExo", podcastExo)
            .add("episode", episode)
            .toString();
    }
}
