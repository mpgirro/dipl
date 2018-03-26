package echo.core.mapper;

import echo.core.domain.dto.ImmutableEpisodeDTO;
import echo.core.domain.dto.ImmutablePodcastDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.entity.Episode;
import echo.core.domain.entity.Podcast;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Mapper(uses = {UrlMapper.class, DateMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TeaserMapper {

    TeaserMapper INSTANCE = Mappers.getMapper( TeaserMapper.class );

    default PodcastDTO asTeaser(PodcastDTO podcast) {
        return Optional.ofNullable(podcast)
            .map(p -> ImmutablePodcastDTO.builder()
                .setEchoId(p.getEchoId())
                .setTitle(p.getTitle())
                .setImage(p.getImage())
                .setLanguage(p.getLanguage())
                .setGenerator(p.getGenerator())
                .setCopyright(p.getCopyright())
                .setEpisodeCount(p.getEpisodeCount())
                .setItunesAuthor(p.getItunesAuthor())
                .setItunesExplicit(p.getItunesExplicit())
                .setItunesBlock(p.getItunesBlock())
                .setRegistrationComplete(p.getRegistrationComplete())
                .setRegistrationTimestamp(p.getRegistrationTimestamp())
                .create())
            .orElse(null);
    }

    default PodcastDTO asTeaser(Podcast podcast) {
        return Optional.ofNullable(podcast)
            .map(p -> ImmutablePodcastDTO.builder()
                .setEchoId(p.getEchoId())
                .setTitle(p.getTitle())
                .setImage(p.getImage())
                .setLanguage(p.getLanguage())
                .setGenerator(p.getGenerator())
                .setCopyright(p.getCopyright())
                .setEpisodeCount(p.getEpisodeCount())
                .setItunesAuthor(p.getItunesAuthor())
                .setItunesExplicit(p.getItunesExplicit())
                .setItunesBlock(p.getItunesBlock())
                .setRegistrationComplete(p.getRegistrationComplete())
                .setRegistrationTimestamp(DateMapper.INSTANCE.asLocalDateTime(p.getRegistrationTimestamp()))
                .create())
            .orElse(null);
    }

    default EpisodeDTO asTeaser(EpisodeDTO episode) {
        return Optional.ofNullable(episode)
            .map(e -> ImmutableEpisodeDTO.builder()
                .setEchoId(e.getEchoId())
                .setTitle(e.getTitle())
                .setPubDate(e.getPubDate())
                .setDescription(e.getDescription())
                .setImage(e.getImage())
                .setItunesDuration(e.getItunesDuration())
                .create())
            .orElse(null);
    }

    default EpisodeDTO asTeaser(Episode episode) {
        return Optional.ofNullable(episode)
            .map(e -> ImmutableEpisodeDTO.builder()
                .setEchoId(e.getEchoId())
                .setTitle(e.getTitle())
                .setPubDate(DateMapper.INSTANCE.asLocalDateTime(e.getPubDate()))
                .setDescription(e.getDescription())
                .setImage(e.getImage())
                .setItunesDuration(e.getItunesDuration())
                .create())
            .orElse(null);
    }

}
