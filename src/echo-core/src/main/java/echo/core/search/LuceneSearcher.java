package echo.core.search;

import echo.core.converter.DocumentConverter;
import echo.core.converter.LuceneEpisodeConverter;
import echo.core.converter.LucenePodcastConverter;
import echo.core.dto.document.DTO;
import echo.core.exception.SearchException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Maximilian Irro
 */
public class LuceneSearcher implements echo.core.search.IndexSearcher{

    private static final Logger log = LoggerFactory.getLogger(LuceneSearcher.class);

    private static final int MAX_RESULT_COUNT = 1000;

    private final SearcherManager searcherManager;
    private final Analyzer analyzer;
    private final MultiFieldQueryParser queryParser;
    /*
    private final ScheduledExecutorService scheduledExecutor;
    private final Future maybeRefreshFuture;
    */
    private final DocumentConverter podcastConverter;
    private final DocumentConverter episodeConverter;

    /**
     *
     * @param indexWriter has to be the same indexWriter used by the Indexer, if search should be performed at the same time as indexing
     * @throws IOException
     */
    public LuceneSearcher(final IndexWriter indexWriter) throws IOException{

        this.searcherManager = new SearcherManager(indexWriter, null);

        this.analyzer = new StandardAnalyzer();
        this.queryParser = new MultiFieldQueryParser(
            new String[] {"title", "description", "link", "website_data"},
            this.analyzer);

        /* TODO this should be better done manually by the actors/microservices
        this.scheduledExecutor = Executors.newScheduledThreadPool(1);
        this.maybeRefreshFuture = this.scheduledExecutor.scheduleWithFixedDelay(() -> {
            try {
                searcherManager.maybeRefresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);
        */

        this.podcastConverter = new LucenePodcastConverter();
        this.episodeConverter = new LuceneEpisodeConverter();
    }

    /**
     *
     * @param q query of the search
     * @param p page of the search window
     * @param s size of the search window
     * @return
     * @throws SearchException if the end of the requested search window (p x s) exceeds the maximum size of retrieved
     *                         documents, or if it exceeds the size of the found documents
     */
    @Override
    public DTO[] search(String q, int p, int s) throws SearchException {

        // ensure that we are within boundries of our search window
        if( (p*s) > MAX_RESULT_COUNT){
            throw new SearchException("Request search range (p x s) exceeds maximum search window s of " + MAX_RESULT_COUNT);
        }

        IndexSearcher indexSearcher = null;
        try {

            final Query query = this.queryParser.parse(q);

//            searcherManager.maybeRefreshBlocking();
            indexSearcher = this.searcherManager.acquire();
            indexSearcher.setSimilarity(new ClassicSimilarity());

            log.debug("Searching for query: "+query.toString());

            final TopDocs topDocs = indexSearcher.search(query, 1);

            if(topDocs.totalHits == 0){
                return new DTO[0];
            }


            if( p > 1 && (p*s) > topDocs.totalHits ){
                throw new SearchException("Request search range (p x s) exceeds amount of found results: " + topDocs.totalHits);
            }

            final ScoreDoc[] hits = indexSearcher.search(query, MAX_RESULT_COUNT).scoreDocs;


            // calculate search window based on page and size
            // ensure that paging does not exceed amount of found results
            final int windowStart = (p-1)*s;
            int windowEnd;
            if((p*s)-1 > topDocs.totalHits){
                windowEnd = (int) topDocs.totalHits;
            } else {
                windowEnd = (p*s)-1;
            }

            int windowSize = windowEnd - windowStart;
            final DTO[] results = new DTO[windowSize];

            for(int i = windowStart; i < windowEnd; i++){
                results[i] = this.toDTO(indexSearcher.doc(hits[i].doc));
            }
            return results;

        } catch (IOException | ParseException e) {
            log.error("Lucene Index has encountered an error searching for: {}", q);
            e.printStackTrace();
            return new DTO[0]; // TODO throw a custom exception, and do not return anything
        } finally {
            if (indexSearcher != null) {
                try {
                    this.searcherManager.release(indexSearcher);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    @Override
    public DTO findByEchoId(String id){
        IndexSearcher indexSearcher = null;
        try {

            final Query query = new TermQuery(new Term("doc_id", id));
            indexSearcher = this.searcherManager.acquire();
            indexSearcher.setSimilarity(new ClassicSimilarity());

            log.debug("Searching for query: "+query.toString());

            final TopDocs topDocs = indexSearcher.search(query, 1);
            if(topDocs.totalHits > 1){
                log.error("Searcher found multiple documents for unique doc_id {}", id);
            }
            if(topDocs.totalHits == 1){
                final ScoreDoc[] hits = indexSearcher.search(query, 1).scoreDocs;
                return this.toDTO(indexSearcher.doc(hits[0].doc));
            }
        } catch (IOException e) {
            log.error("Lucene Index has encountered an error retrieving a Lucene document by id: {}", id);
            e.printStackTrace();
        } finally {
            if (indexSearcher != null) {
                try {
                    this.searcherManager.release(indexSearcher);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }

        return null; // TODO throw a custom exception, and do not return anything
    }

    @Override
    public void refresh(){
        try {
            this.searcherManager.maybeRefreshBlocking();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        try {
//            this.maybeRefreshFuture.cancel(false);
            this.searcherManager.close();
//            this.scheduledExecutor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DTO toDTO(final org.apache.lucene.document.Document doc) {

        // TODO this is not too pretty yet, should have used Scala for this library already...
        if(doc.get("doc_type").equals("podcast")) {
            return this.podcastConverter.toDTO(doc);
        } else if(doc.get("doc_type").equals("episode")) {
            return this.episodeConverter.toDTO(doc);
        } else {
            throw new UnsupportedOperationException("I forgot to support a new document type : " + doc.get("doc_type"));
        }

    }
}
