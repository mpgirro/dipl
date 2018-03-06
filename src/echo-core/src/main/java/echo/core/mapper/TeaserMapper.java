package echo.core.mapper;

import echo.core.domain.entity.Episode;
import echo.core.domain.entity.Podcast;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.PodcastDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses={UrlMapper.class, DateMapper.class})
public interface TeaserMapper {

    TeaserMapper INSTANCE = Mappers.getMapper( TeaserMapper.class );

    default PodcastDTO asTeaser(PodcastDTO dto) {

        if(dto == null) return null;

        final PodcastDTO teaser = new PodcastDTO();

        teaser.setEchoId(dto.getEchoId());
        teaser.setTitle(dto.getTitle());
        teaser.setImage(dto.getImage());
        teaser.setLanguage(dto.getLanguage());
        teaser.setGenerator(dto.getGenerator());
        teaser.setCopyright(dto.getCopyright());
        teaser.setEpisodeCount(dto.getEpisodeCount());
        teaser.setItunesAuthor(dto.getItunesAuthor());
        teaser.setItunesExplicit(dto.getItunesExplicit());
        teaser.setItunesBlock(dto.getItunesBlock());
        teaser.setRegistrationComplete(dto.getRegistrationComplete());
        teaser.setRegistrationTimestamp(dto.getRegistrationTimestamp());

        return teaser;
    }

    default PodcastDTO asTeaser(Podcast podcast) {

        if(podcast == null) return null;

        final PodcastDTO teaser = new PodcastDTO();

        teaser.setEchoId(podcast.getEchoId());
        teaser.setTitle(podcast.getTitle());
        teaser.setImage(podcast.getImage());
        teaser.setLanguage(podcast.getLanguage());

        return teaser;
    }

    default EpisodeDTO asTeaser(EpisodeDTO dto) {

        if(dto == null) return null;

        final EpisodeDTO teaser = new EpisodeDTO();

        teaser.setEchoId(dto.getEchoId());
        teaser.setTitle(dto.getTitle());
        teaser.setPubDate(dto.getPubDate());
        teaser.setDescription(dto.getDescription());
        teaser.setImage(dto.getImage());
        teaser.setItunesDuration(dto.getItunesDuration());

        return teaser;
    }

    default EpisodeDTO asTeaser(Episode episode) {

        if(episode == null) return null;

        final EpisodeDTO teaser = new EpisodeDTO();

        teaser.setEchoId(episode.getEchoId());
        teaser.setTitle(episode.getTitle());
        teaser.setPubDate(DateMapper.INSTANCE.asLocalDateTime(episode.getPubDate()));
        teaser.setDescription(episode.getDescription());
        teaser.setImage(episode.getImage());
        teaser.setItunesDuration(episode.getItunesDuration());

        return teaser;
    }

}
