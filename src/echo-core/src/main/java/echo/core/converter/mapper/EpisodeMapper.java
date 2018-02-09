package echo.core.converter.mapper;

import com.icosillion.podengine.exceptions.DateFormatException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import echo.core.exception.ConversionException;
import echo.core.model.domain.Podcast;
import echo.core.model.dto.EpisodeDTO;
import echo.core.model.domain.Episode;
import org.apache.lucene.document.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={PodcastMapper.class, DateMapper.class})
public interface EpisodeMapper {

    EpisodeMapper INSTANCE = Mappers.getMapper( EpisodeMapper.class );

    @Mappings( {
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "echoId", target = "echoId"),
        @Mapping(source = "podcast.id", target = "podcastId"),
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

    List<EpisodeDTO> episodesToEpisodesDtos(Set<Episode> episodes);

    @Mappings( {
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "echoId", target = "echoId"),
        @Mapping(source = "podcastId", target = "podcast"),
        @Mapping(source = "title", target = "title"),
        @Mapping(source = "link", target = "link"),
        @Mapping(source = "pubDate", target = "pubDate"),
        @Mapping(source = "guid", target = "guid"),
        @Mapping(source = "guidIsPermaLink", target = "guidIsPermaLink"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "itunesImage", target = "itunesImage"),
        @Mapping(source = "itunesDuration", target = "itunesDuration")
    } )
    Episode episodeDtoToEpisode(EpisodeDTO episodeDto);

    Set<Episode> episodeDtosToEpisodes(List<EpisodeDTO> episodeDtos);

    // TODO unused because we use PodcastMapper.class ?
    default Podcast podcastFromId(Long id) {
        if (id == null) {
            return null;
        }
        Podcast podcast = new Podcast();
        podcast.setId(id);
        return podcast;
    }

    default EpisodeDTO luceneDocumentToEpisodeDto(Document doc){

        if (doc == null) {
            return null;
        }

        final EpisodeDTO dto = new EpisodeDTO();

        if(doc.get("echo_id")         != null){ dto.setEchoId(doc.get("echo_id")); }
        if(doc.get("title")           != null){ dto.setTitle(doc.get("title")); }
        if(doc.get("link")            != null){ dto.setLink(doc.get("link")); }
        if(doc.get("pub_date")        != null){ dto.setPubDate(DateMapper.INSTANCE.asLocalDateTime(doc.get("pub_date"))); }
        if(doc.get("guid")            != null){ dto.setGuid(doc.get("guid")); }
        if(doc.get("description")     != null){ dto.setDescription(doc.get("description")); }
        if(doc.get("itunes_image")    != null){ dto.setItunesImage(doc.get("itunes_image")); }
        if(doc.get("itunes_duration") != null){ dto.setItunesDuration(doc.get("itunes_duration")); }

        // note: we do not retrieve websiteData

        return dto;
    }

    List<EpisodeDTO> luceneDocumentsToEpisodeDtos(List<Document> docs);

    default EpisodeDTO podengineEpisodeToEpisodeDto(com.icosillion.podengine.models.Episode episode) throws ConversionException {

        if (episode == null) {
            return null;
        }

        final EpisodeDTO dto = new EpisodeDTO();
        try {
            if(episode.getTitle()       != null){ dto.setTitle(episode.getTitle()); }
            if(episode.getLink()        != null){ dto.setLink(episode.getLink().toExternalForm()); }
            if(episode.getPubDate()     != null){ dto.setPubDate(LocalDateTime.ofInstant(episode.getPubDate().toInstant(), ZoneId.systemDefault())); }
            if(episode.getGUID()        != null){ dto.setGuid(episode.getGUID()); }
            if(episode.getDescription() != null){ dto.setDescription(episode.getDescription()); }
            if(episode.getITunesInfo()  != null){
                if(episode.getITunesInfo().getImageString() != null){ dto.setItunesImage(episode.getITunesInfo().getImageString()); }
                if(episode.getITunesInfo().getDuration()    != null){ dto.setItunesDuration(episode.getITunesInfo().getDuration()); }
            }
        } catch (MalformedFeedException | MalformedURLException | DateFormatException e) {
            throw new ConversionException("Exception during converting podengine.Episode to EpisodeDTO [reason: {}]", e);
        }

        return dto;
    }

    List<EpisodeDTO> podengineEpisodesToEpisodeDtos(List<com.icosillion.podengine.models.Episode> episodes) throws ConversionException;

}
