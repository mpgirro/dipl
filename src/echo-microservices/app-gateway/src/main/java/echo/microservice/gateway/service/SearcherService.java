package echo.microservice.gateway.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import echo.core.benchmark.RoundTripTime;
import echo.core.domain.dto.ImmutableResultWrapperDTO;
import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.microservice.gateway.web.client.SearcherClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Value("${echo.gateway.default-page:1}")
    private Integer DEFAULT_PAGE;

    @Value("${echo.gateway.default-size:20}")
    private Integer DEFAULT_SIZE;

    @Autowired
    private SearcherClient searcherClient;

    public Optional<ResultWrapperDTO> search(String query, Optional<Integer> page, Optional<Integer> size) {
        log.debug("Request to search for query/page/size : ('{}',{},{})", query, page, size);

        final Integer p = page.orElse(DEFAULT_PAGE);
        final Integer s = size.orElse(DEFAULT_SIZE);

        final ResultWrapperDTO result = searcherClient.getSearchResults(query, p, s);

        return Optional.of(result);
    }

    public Optional<ResultWrapperDTO> searchBenchmark(String query, Optional<Integer> page, Optional<Integer> size, RoundTripTime rtt) {
        log.debug("Request to benchmark search for query/page/size : ('{}',{},{})", query, page, size);

        final Integer p = page.orElse(DEFAULT_PAGE);
        final Integer s = size.orElse(DEFAULT_SIZE);

        final ResultWrapperDTO result = searcherClient.getBenchmarkSearchResults(query, p, s, rtt.bumpRTTs());

        return Optional.of(result);
    }

}
