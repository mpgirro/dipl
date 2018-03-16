package echo.core.async.job;

import java.util.Optional;

/**
 * @author Maximilian Irro
 */
public class ParserJob {

    private String exo;
    private String url;
    private String data;

    public String getExo() {
        return exo;
    }

    public void setExo(String exo) {
        this.exo = exo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ParserJob{" +
            "exo='" + exo + '\'' +
            ", url='" + url + '\'' +
            ", data='" + Optional.ofNullable(data).map(d -> "_").orElse("null") + '\'' +
            '}';
    }
}
