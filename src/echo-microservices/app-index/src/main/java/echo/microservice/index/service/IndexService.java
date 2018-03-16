package echo.microservice.index.service;

import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.core.exception.SearchException;
import echo.core.index.IndexCommitter;
import echo.core.index.IndexSearcher;
import echo.core.index.LuceneCommitter;
import echo.core.index.LuceneSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

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

    @PostConstruct
    private void init() throws IOException {
        this.indexCommitter = new LuceneCommitter(INDEX_PATH, true); // TODO do not alway re-create the index
        this.indexSearcher = new LuceneSearcher(((LuceneCommitter) indexCommitter).getIndexWriter());
    }

    public void add(IndexDocDTO doc) {
        log.debug("Request to add document to index : {}", doc);
        indexCommitter.add(doc);
        indexCommitter.commit();
    }

    public ResultWrapperDTO search(String query, Integer page, Integer size) throws SearchException {
        indexSearcher.refresh();
        return indexSearcher.search(query, page, size);
    }
}