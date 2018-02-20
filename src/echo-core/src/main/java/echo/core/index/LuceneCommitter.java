package echo.core.index;

import echo.core.mapper.LuceneMapper;
import echo.core.domain.dto.IndexDocDTO;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Maximilian Irro
 */
public class LuceneCommitter implements IndexCommitter {

    private static final Logger log = LoggerFactory.getLogger(LuceneCommitter.class);

    private final IndexWriter writer;

    public LuceneCommitter(final String indexPath, final boolean create) throws IOException {

        final Directory dir = FSDirectory.open(Paths.get(indexPath));
        final Analyzer analyzer = new StandardAnalyzer();
        final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        if (create) {
            // Create a new index in the directory, removing any
            // previously indexed documents:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else {
            // Add new documents to an existing index:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }

        // Optional: for better indexing performance, if you
        // are indexing many documents, increase the RAM
        // buffer.  But if you do this, increase the max heap
        // size to the JVM (eg add -Xmx512m or -Xmx1g):
        //
        iwc.setRAMBufferSizeMB(256.0);

        this.writer = new IndexWriter(dir, iwc);
        this.writer.commit(); // to create the index if not yet there, and prevent searcher from failing upon creation
    }


    @Override
    public void add(IndexDocDTO indexDoc) {
        log.debug("Appending document to index : {}", indexDoc);

        final Document luceneDoc = LuceneMapper.INSTANCE.map(indexDoc);
        try {
            this.writer.addDocument(luceneDoc);
        } catch (IOException e) {
            log.error("Error adding index entry for : {}" + indexDoc);
            e.printStackTrace();
        }
    }

    @Override
    public void update(IndexDocDTO indexDoc) {
        log.debug("Updating document in index : {}", indexDoc);

        final Document luceneDoc = LuceneMapper.INSTANCE.map(indexDoc);
        try {
            writer.updateDocument(new Term("echo_id", indexDoc.getEchoId()), luceneDoc);
        } catch (IOException e) {
            log.error("Error updating index entry for : {}" + indexDoc);
            e.printStackTrace();
        }
    }

    @Override
    public void commit() {
        log.debug("Committing index");
        try {
            this.writer.commit();
        } catch (IOException e) {
            log.error("error committing index");
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        try {
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IndexWriter getIndexWriter(){
        return this.writer;
    }

    /*
    private Document toLuceneDocument(final DTO dto) {
        if(dto instanceof PodcastDTO){
            return LuceneMapper.INSTANCE.podcastDtoToLuceneDocument((PodcastDTO) dto);
        } else if (dto instanceof EpisodeDTO){
            return LuceneMapper.INSTANCE.episodeDtoToLuceneDocument((EpisodeDTO) dto);
        } else {
            throw new UnsupportedOperationException("I forgot to support a new document type : " + dto.getClass());
        }
    }
    */

}
