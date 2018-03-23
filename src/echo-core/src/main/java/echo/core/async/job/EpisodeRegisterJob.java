package echo.core.async.job;

import echo.core.domain.dto.immutable.TestEpisode;

/**
 * @author Maximilian Irro
 */
public class EpisodeRegisterJob {

    private String podcastExo;
    private TestEpisode episode;

    public String getPodcastExo() {
        return podcastExo;
    }

    public void setPodcastExo(String podcastExo) {
        this.podcastExo = podcastExo;
    }

    public TestEpisode getEpisode() {
        return episode;
    }

    public void setEpisode(TestEpisode episode) {
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
