package echo.core.converter.mapper;

import echo.core.dto.EpisodeDTO;
import echo.core.model.persistence.Episode;
import org.apache.lucene.document.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Maximilian Irro
 */
@Mapper
public interface EpisodeMapper {

    EpisodeMapper INSTANCE = Mappers.getMapper( EpisodeMapper.class );

    @Mappings( {
        @Mapping(source = "echoId", target = "echoId"),
        @Mapping(target = "docId", ignore = true),
        @Mapping(source = "title", target = "title"),
        @Mapping(source = "link", target = "link"),
        @Mapping(source = "pubDate", target = "pubDate"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "itunesImage", target = "itunesImage"),
        @Mapping(source = "itunesDuration", target = "itunesDuration"),
        @Mapping(target = "websiteData", ignore = true)
    } )
    EpisodeDTO episodeToEpisodeDto(Episode episode);

    List<EpisodeDTO> episodesToEpisodesDtos(List<Episode> episodes);

    @Mappings( {
        @Mapping(target = "id", ignore = true),
        @Mapping(source = "echoId", target = "echoId"),
        @Mapping(source = "title", target = "title"),
        @Mapping(source = "link", target = "link"),
        @Mapping(source = "pubDate", target = "pubDate"),
        @Mapping(source = "guid", target = "guid"),
        @Mapping(source = "guidIsPermaLink", target = "guidIsPermaLink"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "itunesImage", target = "itunesImage"),
        @Mapping(source = "itunesDuration", target = "itunesDuration")
    } )
    Episode episodeDtoToEpisode(Episode episodeDto);

    List<Episode> episodeDtosToEpisodes(List<EpisodeDTO> episodeDtos);

    default EpisodeDTO luceneDocumentToEpisodeDto(Document doc){

        if(doc == null){
            return null;
        }

        final EpisodeDTO dto = new EpisodeDTO();

        if(doc.get("echo_id")         != null){ dto.setDocId(doc.get("echo_id")); }
        if(doc.get("doc_id")          != null){ dto.setDocId(doc.get("doc_id")); }
        if(doc.get("title")           != null){ dto.setTitle(doc.get("title")); }
        if(doc.get("link")            != null){ dto.setLink(doc.get("link")); }
        if(doc.get("pub_date")        != null){ dto.setPubDate(LocalDateTimeMapper.INSTANCE.asLocalDateTime(doc.get("pub_date"))); }
        if(doc.get("guid")            != null){ dto.setGuid(doc.get("guid")); }
        if(doc.get("description")     != null){ dto.setDescription(doc.get("description")); }
        if(doc.get("itunes_image")    != null){ dto.setItunesImage(doc.get("itunes_image")); }
        if(doc.get("itunes_duration") != null){ dto.setItunesDuration(doc.get("itunes_duration")); }

        // note: we do not retrieve websiteData

        return dto;
    }

    List<EpisodeDTO> luceneDocumentsToEpisodeDtos(List<Document> docs);

}
