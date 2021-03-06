package echo.microservice.searcher.service;

import echo.core.benchmark.rtt.RoundTripTime;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.microservice.searcher.web.client.IndexClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Maximilian Irro
 */
@Service
public class SearchService {

    private final Logger log = LoggerFactory.getLogger(SearchService.class);

    @Value("${echo.searcher.default-page:1}")
    private Integer DEFAULT_PAGE;

    @Value("${echo.searcher.default-size:20}")
    private Integer DEFAULT_SIZE;

    @Autowired
    private IndexClient indexClient;

    public ResultWrapperDTO search(String query, Optional<Integer> page, Optional<Integer> size) {
        log.debug("Request to search for query/page/size : ('{}',{},{})", query, page, size);

        final int p = page.orElse(DEFAULT_PAGE);
        final int s = size.orElse(DEFAULT_SIZE);

        if (isNullOrEmpty(query)) return ResultWrapperDTO.empty();
        if (p < 0) return ResultWrapperDTO.empty();
        if (s < 0) return ResultWrapperDTO.empty();

        return indexClient.getSearchResults(query, p, s);
    }

    public ResultWrapperDTO searchBenchmark(String query, Optional<Integer> page, Optional<Integer> size, RoundTripTime rtt) {
        log.debug("Request to benchmark search for query/page/size : ('{}',{},{})", query, page, size);

        final int p = page.orElse(DEFAULT_PAGE);
        final int s = size.orElse(DEFAULT_SIZE);

        if (isNullOrEmpty(query)) {
            log.warn("Query is empty");
            return ResultWrapperDTO.empty();
        }
        if (p < 0) {
            log.warn("Parameter 'p' is < 0");
            return ResultWrapperDTO.empty();
        }
        if (s < 0) {
            log.warn("Parameter 's' is < 0");
            return ResultWrapperDTO.empty();
        }

        return indexClient.getBenchmarkSearchResults(query, p, s, rtt.bumpRTTs());
    }

}
