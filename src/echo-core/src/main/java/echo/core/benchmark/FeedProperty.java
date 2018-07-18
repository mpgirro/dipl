package echo.core.benchmark;

import com.google.gson.Gson;

/**
 * @author Maximilian Irro
 */
public class FeedProperty {

    private final String uri;
    private final String location;
    private final Integer numberOfEpisodes;

    public FeedProperty(String uri, String location, Integer numberOfEpisodes) {
        this.uri = uri;
        this.location = location;
        this.numberOfEpisodes = numberOfEpisodes;
    }

    public String getUri() {
        return uri;
    }

    public String getLocation() {
        return location;
    }

    public Integer getNumberOfEpisodes() {
        return numberOfEpisodes;
    }

}
