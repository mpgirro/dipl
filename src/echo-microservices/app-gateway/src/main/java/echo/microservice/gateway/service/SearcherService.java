package echo.microservice.gateway.service;

import com.google.common.collect.Lists;
import echo.core.domain.dto.ResultWrapperDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Service
public class SearcherService {

    private final Logger log = LoggerFactory.getLogger(SearcherService.class);

    private final String SEARCHER_URL = "http://localhost:3035/searcher";

    private RestTemplate restTemplate = new RestTemplate();

    public Optional<ResultWrapperDTO> search(String query, Integer page, Integer size) {
        log.debug("Request to search for query/page/size : ('{}',{},{})", query, page, size);

        String url = SEARCHER_URL+"/search?query="+query;
        if (page != null) url += "&page="+page;
        if (size != null) url += "&size="+size;

        //and do I need this JSON media type for my use case?
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<ResultWrapperDTO> entity = new HttpEntity<>(headers);

        final ResponseEntity<ResultWrapperDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, ResultWrapperDTO.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.ofNullable(response.getBody());
        } else {
            log.warn("Got status from Catalog : {}", response.getStatusCode().value());
            return Optional.empty();
        }
    }
}
