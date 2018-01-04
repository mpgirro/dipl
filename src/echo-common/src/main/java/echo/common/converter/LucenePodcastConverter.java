package echo.common.converter;

import echo.common.dto.document.PodcastDocument;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/**
 * @author Maximilian Irro
 */
public class LucenePodcastConverter extends DocumentConverter<PodcastDocument,org.apache.lucene.document.Document> {

    @Override
    public PodcastDocument toEchoDocument(Document doc) {
        throw new UnsupportedOperationException("LucenePodcastConverter.toEchoDocument() not yet implemented");
    }

    @Override
    public Document toIndexDocument(PodcastDocument podcast) {
        final Document doc = new Document();

        doc.add(new TextField("title", podcast.getTitle(), Field.Store.YES));
        doc.add(new TextField("link", podcast.getLink(), Field.Store.YES));
        doc.add(new TextField("description", podcast.getDescription(), Field.Store.YES));

        // TODO
        //doc.add(new StringField("lastBuildDate", podcast.getLastBuildDate().toString(), Field.Store.YES));

        doc.add(new StringField("language", podcast.getLanguage(), Field.Store.YES));

        if(podcast.getGenerator() != null){
            // TODO know to be found as null in:
            // - http://www.fanboys.fm/episodes.mp3.rss
            doc.add(new TextField("generator", podcast.getGenerator(), Field.Store.YES));
        }


        return doc;
    }
}
