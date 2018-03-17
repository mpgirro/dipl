package echo.core.async.job;

import echo.core.domain.dto.EpisodeDTO;

/**
 * @author Maximilian Irro
 */
public class EpisodeRegisterJob {

    private String podcastExo;
    private EpisodeDTO episode;

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
        return "EpisodeRegisterJob{" +
            "podcastExo='" + podcastExo + '\'' +
            ", episode=" + episode +
            '}';
    }
}
