package echo.core.converter.mapper;

import echo.core.dto.EpisodeDTO;
import echo.core.dto.PodcastDTO;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Maximilian Irro
 */
@Mapper(uses=LocalDateTimeMapper.class)
public interface LuceneMapper {

    LuceneMapper INSTANCE = Mappers.getMapper( LuceneMapper.class );

    default Document podcastDtoToLuceneDocument(PodcastDTO podcast){

        if(podcast == null){
            return null;
        }

        final Document doc = new Document();
        doc.add(new StringField("doc_type", "podcast", Field.Store.YES));

        if(podcast.getEchoId()         != null){ doc.add(new StringField("echo_id", podcast.getEchoId(), Field.Store.YES)); }
        if(podcast.getDocId()          != null){ doc.add(new StringField("doc_id", podcast.getDocId(), Field.Store.YES)); }
        if(podcast.getTitle()          != null){ doc.add(new TextField("title", podcast.getTitle(), Field.Store.YES)); }
        if(podcast.getLink()           != null){ doc.add(new TextField("link", podcast.getLink(), Field.Store.YES)); }
        if(podcast.getDescription()    != null){ doc.add(new TextField("description", podcast.getDescription(), Field.Store.YES)); }
        if(podcast.getPubDate()        != null){ doc.add(new StringField("pub_date", LocalDateTimeMapper.INSTANCE.asString(podcast.getPubDate()), Field.Store.YES)); }
        if(podcast.getLastBuildDate()  != null){ doc.add(new StringField("last_build_date", LocalDateTimeMapper.INSTANCE.asString(podcast.getLastBuildDate()), Field.Store.YES)); }
        if(podcast.getLanguage()       != null){ doc.add(new StringField("language", podcast.getLanguage(), Field.Store.YES)); }
        if(podcast.getGenerator()      != null){ doc.add(new TextField("generator", podcast.getGenerator(), Field.Store.YES)); }
        if(podcast.getItunesImage()    != null){ doc.add(new TextField("itunes_image", podcast.getItunesImage(), Field.Store.YES)); }
        if(podcast.getItunesCategory() != null){ doc.add(new TextField("itunes_category", podcast.getItunesCategory(), Field.Store.YES)); }

        if(podcast.getWebsiteData()    != null){ doc.add(new TextField("website_data", podcast.getWebsiteData(), Field.Store.NO)); }

        return doc;
    }

    List<Document> podcastDtosToLuceneDocuments(List<PodcastDTO> podcastDtos);

    default Document episodeDtoToLuceneDocument(EpisodeDTO episode){
        if(episode == null){
            return null;
        }

        final Document doc = new Document();
        doc.add(new StringField("doc_type", "episode", Field.Store.YES));

        if(episode.getEchoId()         != null){ doc.add(new StringField("echo_id", episode.getEchoId(), Field.Store.YES)); }
        if(episode.getDocId()          != null){ doc.add(new StringField("doc_id", episode.getDocId(), Field.Store.YES)); }
        if(episode.getTitle()          != null){ doc.add(new TextField("title", episode.getTitle(), Field.Store.YES)); }
        if(episode.getLink()           != null){ doc.add(new TextField("link", episode.getLink(), Field.Store.YES)); }
        if(episode.getPubDate()        != null){ doc.add(new StringField("pub_date", episode.getPubDate().toString(), Field.Store.YES)); }
        if(episode.getGuid()           != null){ doc.add(new StringField("guid", episode.getGuid(), Field.Store.YES)); }
        if(episode.getDescription()    != null){ doc.add(new TextField("description", episode.getDescription(), Field.Store.YES)); }
        if(episode.getItunesImage()    != null){ doc.add(new TextField("itunes_image", episode.getItunesImage(), Field.Store.YES)); }
        if(episode.getItunesDuration() != null){ doc.add(new TextField("itunes_duration", episode.getItunesDuration(), Field.Store.YES)); }

        if(episode.getWebsiteData()    != null){ doc.add(new TextField("website_data", episode.getWebsiteData(), Field.Store.NO)); }

        return doc;
    }

    List<Document> episodeDtosToLuceneDocuments(List<EpisodeDTO> episodeDtos);

}
