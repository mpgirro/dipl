package echo.microservice.index.service;

import echo.core.benchmark.RoundTripTime;
import echo.core.domain.dto.ImmutableResultWrapperDTO;
import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.core.exception.SearchException;
import echo.core.index.IndexCommitter;
import echo.core.index.IndexSearcher;
import echo.core.index.LuceneCommitter;
import echo.core.index.LuceneSearcher;
import echo.microservice.index.web.client.BenchmarkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Service
public class IndexService {

    private final Logger log = LoggerFactory.getLogger(IndexService.class);

    @Value("${echo.index.lucene-path:index}")
    private String INDEX_PATH;

    private IndexCommitter indexCommitter;
    private IndexSearcher indexSearcher;

    private final List<IndexDocDTO> cache = new LinkedList<>();

    @Autowired
    private BenchmarkClient benchmarkClient;

    @PostConstruct
    private void init() throws IOException {
        this.indexCommitter = new LuceneCommitter(INDEX_PATH, true); // TODO do not alway re-create the index
        this.indexSearcher = new LuceneSearcher(((LuceneCommitter) indexCommitter).getIndexWriter());
    }

    @PreDestroy
    private void destroy() {
        Optional.ofNullable(indexCommitter).ifPresent(IndexCommitter::destroy);
        Optional.ofNullable(indexSearcher).ifPresent(IndexSearcher::destroy);
    }

    public void add(IndexDocDTO doc) {
        log.info("Request to add document to index cache : {}", doc.getExo());
        synchronized (cache) {
            cache.add(doc);
        }
        //indexCommitter.add(doc);
        //indexCommitter.commit();
    }

    public ResultWrapperDTO search(String query, Integer page, Integer size, RoundTripTime rtt) throws SearchException {
        //final long beforeRefresh = System.currentTimeMillis();
        indexSearcher.refresh();
        //final long afterRefresh = System.currentTimeMillis();
        final ImmutableResultWrapperDTO result = (ImmutableResultWrapperDTO) indexSearcher.search(query, page, size);
        //final long afterSearch = System.currentTimeMillis();
        //log.info("[BENCH] Refresh took : {}ms ; Search took : {}ms", afterRefresh-beforeRefresh, afterSearch-afterRefresh);
        return result.withRTT(rtt.bumpRTTs());
    }

    @Scheduled(fixedDelay = 3000)
    public void commitIndex() {
        synchronized (cache) {
            if (!cache.isEmpty()) {
                log.info("Adding cached documents to index and executing commit");
                for (IndexDocDTO doc : cache) {
                    indexCommitter.add(doc);
                }
                cache.clear();
                indexCommitter.commit();
            }
        }
    }
}
