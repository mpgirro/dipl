package echo.core.index;

import echo.core.mapper.EpisodeMapper;
import echo.core.mapper.PodcastMapper;
import echo.core.mapper.IndexDocMapper;
import echo.core.model.dto.IndexDocDTO;
import echo.core.model.dto.ResultWrapperDTO;
import echo.core.exception.SearchException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Maximilian Irro
 */
public class LuceneSearcher implements echo.core.index.IndexSearcher {

    private static final Logger log = LoggerFactory.getLogger(LuceneSearcher.class);

    private static final int MAX_RESULT_COUNT = 1000;

    private final SearcherManager searcherManager;
    private final Analyzer analyzer;
    private final MultiFieldQueryParser queryParser;

    /**
     *
     * @param indexWriter has to be the same indexWriter used by the Indexer, if search should be performed at the same time as indexing
     * @throws IOException
     */
    public LuceneSearcher(final IndexWriter indexWriter) throws IOException{

        this.searcherManager = new SearcherManager(indexWriter, null);

        this.analyzer = new StandardAnalyzer();
        this.queryParser = new MultiFieldQueryParser(
            new String[] {"title", "description", "link", "content_encoded", "website_data"},
            this.analyzer);
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
    public ResultWrapperDTO search(String q, int p, int s) throws SearchException {

        if( p < 1 ){
            throw new SearchException("Requested page number (p) required to be >1, got: " + p);
        }

        if( s < 1 ){
            throw new SearchException("Requested window size (s) required to be >1, got: " + s);
        }

        // ensure that we are within boundries of our search window
        if( (p*s) > MAX_RESULT_COUNT){
            throw new SearchException("Request search range (p x s) exceeds maximum search window s of " + MAX_RESULT_COUNT);
        }

        final ResultWrapperDTO resultWrapper = new ResultWrapperDTO();

        // set some sane values, we'll overwrite these if all goes well
        resultWrapper.setCurrPage(0);
        resultWrapper.setMaxPage(0);
        resultWrapper.setTotalHits(0);

        IndexSearcher indexSearcher = null;
        try {

            final Query query = this.queryParser.parse(q);

            indexSearcher = this.searcherManager.acquire();
            indexSearcher.setSimilarity(new ClassicSimilarity());

            log.debug("Searching for query: "+query.toString());

            final TopDocs topDocs = indexSearcher.search(query, 1);

            if(topDocs.totalHits == 0){
                return resultWrapper;
            }

            final ScoreDoc[] hits = indexSearcher.search(query, MAX_RESULT_COUNT).scoreDocs;

            resultWrapper.setCurrPage(p);

            final double dMaxPage = ((double)topDocs.totalHits) / ((double) s);
            final int maxPage = (int) Math.ceil(dMaxPage);
            if(maxPage == 0 && resultWrapper.getCurrPage() == 1){
                resultWrapper.setMaxPage(1);
            } else {
                resultWrapper.setMaxPage(maxPage);
            }
            resultWrapper.setTotalHits((int) topDocs.totalHits);

            // calculate search window based on page and size
            // ensure that paging does not exceed amount of found results
            final int windowStart = (p-1)*s;
            int windowEnd;
            if((p*s) > topDocs.totalHits){
                windowEnd = (int) topDocs.totalHits;
            } else {
                windowEnd = (p*s);
            }

            int windowSize = windowEnd - windowStart;
            final IndexDocDTO[] results = new IndexDocDTO[windowSize];

            int j = 0;
            for(int i = windowStart; i < windowEnd; i++){
                results[j] = toIndexDoc(indexSearcher.doc(hits[i].doc));
                j += 1;
            }

            resultWrapper.setResults(results);
            return resultWrapper;

        } catch (IOException | ParseException e) {
            log.error("Lucene Index has encountered an error searching for: {}", q);
            e.printStackTrace();
            return resultWrapper; // TODO throw a custom exception, and do not return anything
        } finally {
            if (indexSearcher != null) {
                try {
                    this.searcherManager.release(indexSearcher);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    @Override
    public IndexDocDTO findByEchoId(String id){
        IndexSearcher indexSearcher = null;
        try {

            final Query query = new TermQuery(new Term("echo_id", id));
            indexSearcher = this.searcherManager.acquire();
            indexSearcher.setSimilarity(new ClassicSimilarity());

            log.debug("Searching for query: "+query.toString());

            final TopDocs topDocs = indexSearcher.search(query, 1);
            if(topDocs.totalHits > 1){
                log.error("Searcher found multiple documents for unique echo_id {}", id);
            }
            if(topDocs.totalHits == 1){
                final ScoreDoc[] hits = indexSearcher.search(query, 1).scoreDocs;
                return toIndexDoc(indexSearcher.doc(hits[0].doc));
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
            this.searcherManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    private DTO toDTO(Document doc) {
        if(doc.get("doc_type").equals("podcast")) {
            return PodcastMapper.INSTANCE.luceneDocumentToPodcastDto(doc);
        } else if(doc.get("doc_type").equals("episode")) {
            return EpisodeMapper.INSTANCE.luceneDocumentToEpisodeDto(doc);
        } else {
            throw new UnsupportedOperationException("I forgot to support a new document type : " + doc.get("doc_type"));
        }
    }
    */

    private IndexDocDTO toIndexDoc(Document doc){
        if(doc.get("doc_type").equals("podcast")) {
            return IndexDocMapper.INSTANCE.podcastDtoToIndexResult(PodcastMapper.INSTANCE.luceneDocumentToPodcastDto(doc));
        } else if(doc.get("doc_type").equals("episode")) {
            return IndexDocMapper.INSTANCE.episodeDtoToIndexResult(EpisodeMapper.INSTANCE.luceneDocumentToEpisodeDto(doc));
        } else {
            throw new UnsupportedOperationException("I forgot to support a new document type : " + doc.get("doc_type"));
        }
    }
}
