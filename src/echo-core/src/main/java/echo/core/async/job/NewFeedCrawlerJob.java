package echo.core.async.job;

import com.google.common.base.MoreObjects;

/**
 * @author Maximilian Irro
 */
public class NewFeedCrawlerJob implements CrawlerJob {

    private String exo;
    private String url;

    public NewFeedCrawlerJob() {

    }

    public NewFeedCrawlerJob(String exo, String url) {
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
        return MoreObjects.toStringHelper("NewFeedCrawlerJob")
            .omitNullValues()
            .add("exo", exo)
            .add("url", url)
            .toString();
    }

}
