package echo.core.async.job;

/**
 * @author Maximilian Irro
 */
public class UpdateFeedCrawlerJob implements CrawlerJob {

    private String exo;
    private String url;

    public UpdateFeedCrawlerJob() {

    }

    public UpdateFeedCrawlerJob(String exo, String url) {
        this.exo = exo;
        this.url = url;
    }

    @Override
    public String getExo() {
        return exo;
    }

    public void setExo(String exo) {
        this.exo = exo;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "UpdateFeedCrawlerJob{" +
            "exo='" + exo + '\'' +
            ", url='" + url + '\'' +
            '}';
    }

}
