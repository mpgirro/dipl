package echo.common.search;

import echo.common.converter.DocumentConverter;
import echo.common.converter.LuceneEpisodeConverter;
import echo.common.converter.LucenePodcastConverter;
import echo.common.dto.document.Document;
import echo.common.dto.document.EpisodeDocument;
import echo.common.dto.document.PodcastDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Maximilian Irro
 */
public class LuceneSearcher implements echo.common.search.IndexSearcher{

    private IndexReader indexReader;
    private org.apache.lucene.search.IndexSearcher indexSearcher;
    private Analyzer analyzer;
    private MultiFieldQueryParser queryParser;

    private final DocumentConverter podcastConverter;
    private final DocumentConverter episodeConverter;

    public LuceneSearcher(final String indexPath) throws IOException{

        // log.info("Using index: " + FSDirectory.open(Paths.get(index)).getDirectory().toAbsolutePath().toString());
        this.indexReader= DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));

        this.indexSearcher = new org.apache.lucene.search.IndexSearcher(indexReader);

        // log.info("Using Lucene Default Similarity");
        //indexSearcher.setSimilarity(new ClassicSimilarity());

        this.analyzer = new StandardAnalyzer();
        this.queryParser = new MultiFieldQueryParser(
            new String[] {"title", "description"},
            analyzer);


        this.podcastConverter = new LucenePodcastConverter();
        this.episodeConverter = new LuceneEpisodeConverter();
    }

    @Override
    public Document[] search(String queryStr) {

        try {

            final Query query = this.queryParser.parse(queryStr);

            /*
            final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
            booleanQueryBuilder.add(query, BooleanClause.Occur.SHOULD);

            booleanQueryBuilder.add(query2, BooleanClause.Occur.MUST);
            booleanQueryBuilder.add(query3, BooleanClause.Occur.MUST);
            booleanQueryBuilder.add(query4, BooleanClause.Occur.MUST);

            query = booleanQueryBuilder.build();

            //Query query = new TermQuery(new Term(field, queryString));
            */

//        log.info("Searching for: " + query.toString(field));

            System.out.println("SearchIndex has currentl "+indexReader.numDocs()+" documents");

            final ScoreDoc[] hits = this.indexSearcher.search(query, 1000).scoreDocs;

            final Document[] results = new Document[hits.length];
            for(int i = 0; i < hits.length; i++){
                results[i] = this.toEchoDocument(this.indexSearcher.doc(hits[i].doc));
            }
            return results;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return new Document[0];
        }

    }

    @Override
    public void close() {
        try {
            this.indexReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private echo.common.dto.document.Document toEchoDocument(final org.apache.lucene.document.Document doc) {

        // TODO this is not too pretty yet, should have used Scala for this library already...
        if(doc.get("doc_type").equals("podcast")) {
            return this.podcastConverter.toEchoDocument(doc);
        } else if(doc.get("doc_type").equals("podcast")) {
            return this.episodeConverter.toEchoDocument(doc);
        } else {
            throw new UnsupportedOperationException("I forgot to support a new document type : " + doc.get("doc_type"));
        }

    }
}
