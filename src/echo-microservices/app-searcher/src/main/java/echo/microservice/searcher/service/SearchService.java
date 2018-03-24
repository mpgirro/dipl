package echo.microservice.searcher.service;

import echo.core.domain.dto.ResultWrapperDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Service
public class SearchService {

    private final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final String INDEX_URL = "http://localhost:3032"; // TODO

    @Value("${echo.searcher.default-page:1}")
    private Integer DEFAULT_PAGE;

    @Value("${echo.searcher.default-size:20}")
    private Integer DEFAULT_SIZE;

    private final RestTemplate restTemplate = new RestTemplate();

    public ResultWrapperDTO search(String query, Integer page, Integer size) {

        final int p = Optional.ofNullable(page).orElse(DEFAULT_PAGE);
        final int s = Optional.ofNullable(size).orElse(DEFAULT_SIZE);

        if (p < 0) return ResultWrapperDTO.empty();
        if (s < 0) return ResultWrapperDTO.empty();

        // TODO do not hardcode this
        final String url = INDEX_URL+"/index/search?query="+query+"&page="+p+"&size="+s;

        return restTemplate.getForObject(url, ResultWrapperDTO.class);
    }

}
