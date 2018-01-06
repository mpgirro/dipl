package echo.core.search;

import echo.core.converter.DocumentConverter;
import echo.core.converter.LuceneEpisodeConverter;
import echo.core.converter.LucenePodcastConverter;
import echo.core.dto.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.ClassicSimilarity;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Maximilian Irro
 */
public class LuceneSearcher implements echo.core.search.IndexSearcher{

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
            new String[] {"title", "description", "link"},
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

    @Override
    public Document[] search(String queryStr) {

        IndexSearcher indexSearcher = null;
        try {

            final Query query = this.queryParser.parse(queryStr);

//            searcherManager.maybeRefreshBlocking();
            indexSearcher = this.searcherManager.acquire();
            indexSearcher.setSimilarity(new ClassicSimilarity());

//            log.info("Searching for query: "+query.toString());

            final TopDocs topDocs = indexSearcher.search(query, 1);
            if(topDocs.totalHits > 0){
                final ScoreDoc[] hits = indexSearcher.search(query, 1000).scoreDocs;
                final Document[] results = new Document[hits.length];
                for(int i = 0; i < hits.length; i++){
                    results[i] = this.toEchoDocument(indexSearcher.doc(hits[i].doc));
                }
                return results;
            } else {
                return new Document[0];
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return new Document[0];
        } finally {
            if (indexSearcher != null) {
                try {
                    this.searcherManager.release(indexSearcher);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
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

    private echo.core.dto.document.Document toEchoDocument(final org.apache.lucene.document.Document doc) {

        // TODO this is not too pretty yet, should have used Scala for this library already...
        if(doc.get("doc_type").equals("podcast")) {
            return this.podcastConverter.toEchoDocument(doc);
        } else if(doc.get("doc_type").equals("episode")) {
            return this.episodeConverter.toEchoDocument(doc);
        } else {
            throw new UnsupportedOperationException("I forgot to support a new document type : " + doc.get("doc_type"));
        }

    }
}
