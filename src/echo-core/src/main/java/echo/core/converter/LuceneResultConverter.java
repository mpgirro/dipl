package echo.core.converter;

import echo.core.dto.document.IndexResult;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class LuceneResultConverter extends ResultConverter<Document> {

    @Override
    public IndexResult toResult(Document doc) {

        final IndexResult dto = new IndexResult();

        if(doc.get("doc_type") != null){
            final String type = doc.get("doc_type");
            if(type.equals("podcast")){
                dto.setDocType("podcast");
            } else if(type.equals("episode")){
                dto.setDocType("episode");
            } else {
                throw new RuntimeException("I forgot to support a new document type : " + type);
            }
        } else {
            throw new RuntimeException("I forgot to set the 'doc_type' field before saving to index");
        }
        if(doc.get("doc_id")          != null){ dto.setEchoId(doc.get("doc_id")); } // TODO change to echo_id
        if(doc.get("title")           != null){ dto.setTitle(doc.get("title")); }
        if(doc.get("link")            != null){ dto.setLink(doc.get("link")); }
        if(doc.get("pub_date")        != null){ dto.setPubDate(LocalDateTime.parse(doc.get("pub_date"))); }
        if(doc.get("description")     != null){ dto.setDescription(doc.get("description")); }
        if(doc.get("itunes_image")    != null){ dto.setItunesImage(doc.get("itunes_image")); }
        //if(lDoc.get("itunes_duration") != null){ dto.setItunesDuration(lDoc.get("itunes_duration")); } // TODO

        // note: we do not retrieve websiteData

        return dto;
    }

    /* TODO delete?
    @Override
    public Document toIndex(IndexResult dto) {

        final Document doc = new Document();
        doc.add(new StringField("type", "episode", Field.Store.YES));

//        if(episode.getEchoId()         != null){ doc.add(new StringField("echo_id", episode.getEchoId(), Field.Store.YES)); } // TODO
        if(dto.getEchoId()          != null){ doc.add(new StringField("doc_id", dto.getEchoId(), Field.Store.YES)); } // TODO change to echo_id
        if(dto.getTitle()          != null){ doc.add(new TextField("title", dto.getTitle(), Field.Store.YES)); }
        if(dto.getLink()           != null){ doc.add(new TextField("link", dto.getLink(), Field.Store.YES)); }
        if(dto.getPubDate()        != null){ doc.add(new StringField("pub_date", dto.getPubDate().toString(), Field.Store.YES)); }
        if(dto.getDescription()    != null){ doc.add(new TextField("description", dto.getDescription(), Field.Store.YES)); }
        if(dto.getItunesImage()    != null){ doc.add(new TextField("itunes_image", dto.getItunesImage(), Field.Store.YES)); }
        //if(dto.getItunesDuration() != null){ doc.add(new TextField("itunes_duration", dto.getItunesDuration(), Field.Store.YES)); } // TODO

        //if(episode.getWebsiteData()    != null){ doc.add(new TextField("website_data", episode.getWebsiteData(), Field.Store.NO)); } // TODO

        return doc;
    }
    */
}
