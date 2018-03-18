package echo.microservice.gateway.web.dto;

import java.util.List;

/**
 * Generic Wrapper class for jSON serialized Lists, required to protect against
 * JSON Hijacking for Older Browsers: Always return JSON with an Object on the outside
 *
 * @author Maximilian Irro
 */
public class ArrayWrapperDTO<T> {

    private List<T> results;

    public ArrayWrapperDTO() { }

    public ArrayWrapperDTO(List<T> results) {
        this.results = results;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }
}
