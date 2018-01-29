package echo.core.converter.mapper;

import echo.core.model.dto.PodcastDTO;
import echo.core.model.domain.Podcast;
import org.apache.lucene.document.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Maximilian Irro
 */
@Mapper(uses=UrlMapper.class)
public interface PodcastMapper {

    PodcastMapper INSTANCE = Mappers.getMapper( PodcastMapper.class );

    @Mappings( {
        @Mapping(source = "echoId", target = "echoId"),
        @Mapping(source = "title", target = "title"),
        @Mapping(source = "link", target = "link"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "pubDate", target = "pubDate"),
        @Mapping(source = "lastBuildDate", target = "lastBuildDate"),
        @Mapping(source = "language", target = "language"),
        @Mapping(source = "generator", target = "generator"),
        @Mapping(source = "itunesImage", target = "itunesImage"),
        @Mapping(source = "itunesCategory", target = "itunesCategory"),
        @Mapping(target = "websiteData", ignore = true)
    } )
    PodcastDTO podcastToPodcastDto(Podcast podcast);

    List<PodcastDTO> podcastsToPodcastDtos(List<Podcast> podcasts);

    @Mappings( {
        @Mapping(target = "id", ignore = true),
        @Mapping(source = "echoId", target = "echoId"),
        @Mapping(source = "title", target = "title"),
        @Mapping(source = "link", target = "link"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "pubDate", target = "pubDate"),
        @Mapping(source = "lastBuildDate", target = "lastBuildDate"),
        @Mapping(source = "language", target = "language"),
        @Mapping(source = "generator", target = "generator"),
        @Mapping(source = "itunesImage", target = "itunesImage"),
        @Mapping(source = "itunesCategory", target = "itunesCategory")
    } )
    Podcast podcastDtoToPodcast(Podcast podcastDto);

    List<Podcast> podcastDtosToPodcasts(List<PodcastDTO> podcastDtos);

    default PodcastDTO luceneDocumentToPodcastDto(Document doc){

        if(doc == null){
            return null;
        }

        final PodcastDTO dto = new PodcastDTO();

        if(doc.get("echo_id")         != null){ dto.setEchoId(doc.get("echo_id")); }
        if(doc.get("title")           != null){ dto.setTitle(doc.get("title")); }
        if(doc.get("link")            != null){ dto.setLink(doc.get("link")); }
        if(doc.get("description")     != null){ dto.setDescription(doc.get("description")); }
        if(doc.get("pub_date")        != null){ dto.setLastBuildDate(LocalDateTimeMapper.asLocalDateTime(doc.get("pub_date"))); }
        if(doc.get("last_build_date") != null){ dto.setLastBuildDate(LocalDateTimeMapper.asLocalDateTime(doc.get("last_build_date"))); }
        if(doc.get("language")        != null){ dto.setLanguage(doc.get("language")); }
        if(doc.get("generator")       != null){ dto.setGenerator(doc.get("generator")); }
        if(doc.get("itunes_image")    != null){ dto.setItunesImage(doc.get("itunes_image")); }
        if(doc.get("itunes_category") != null){ dto.setItunesCategory(doc.get("itunes_category")); }

        // note: we do not retrieve websiteData

        return dto;
    }

    List<PodcastDTO> luceneDocumentsToPodcastDtos(List<Document> docs);

}
