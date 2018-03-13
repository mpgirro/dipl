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

    @Value("${echo.gateway.default-page:1}")
    private Integer DEFAULT_PAGE;

    @Value("${echo.gateway.default-size:20}")
    private Integer DEFAULT_SIZE;

    private RestTemplate restTemplate = new RestTemplate();

    public ResultWrapperDTO search(String query, Integer page, Integer size) {

        final int p = Optional.ofNullable(page).orElse(page);
        final int s = Optional.ofNullable(size).orElse(size);

        if (p < 0) return new ResultWrapperDTO();
        if (s < 0) return new ResultWrapperDTO();

        // TODO do not hardcode this
        final String url = "http://localhost:3032/api/index/search?query="+query+"&page="+p+"&size="+s;

        return restTemplate.getForObject(url, ResultWrapperDTO.class);
    }

}
