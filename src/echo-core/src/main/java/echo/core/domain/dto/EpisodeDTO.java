package echo.core.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Maximilian Irro
 */
@Value.Immutable
@Value.Modifiable                   // generates implementation with setters, required by mappers
@Value.Style(
    jdkOnly    = true,              // prevent usage of Guava collections
    get        = {"is*", "get*"},   // Detect 'get' and 'is' prefixes in accessor methods
    init       = "set*",
    create     = "new",             // generates public no args constructor
    build      = "create"           // rename 'build' method on builder to 'create'
)
@JsonSerialize(as = ImmutableEpisodeDTO.class)
@JsonDeserialize(as = ImmutableEpisodeDTO.class)
public interface EpisodeDTO {

    @Nullable
    Long getId();

    @Nullable
    Long getPodcastId();

    @Nullable
    String getExo();

    @Nullable
    String getPodcastExo();

    @Nullable
    String getPodcastTitle();

    @Nullable
    String getTitle();

    @Nullable
    String getLink();

    @Nullable
    LocalDateTime getPubDate();

    @Nullable
    String getGuid();

    @Nullable
    Boolean getGuidIsPermaLink();

    @Nullable
    String getDescription();

    @Nullable
    String getImage();

    @Nullable
    String getItunesDuration();

    @Nullable
    String getItunesSubtitle();

    @Nullable
    String getItunesAuthor();

    @Nullable
    String getItunesSummary();

    @Nullable
    Integer getItunesSeason();

    @Nullable
    Integer getItunesEpisode();

    @Nullable
    String getItunesEpisodeType();

    @Nullable
    String getEnclosureUrl();

    @Nullable
    Long getEnclosureLength();

    @Nullable
    String getEnclosureType();

    @Nullable
    String getContentEncoded();

    @Nullable
    LocalDateTime getRegistrationTimestamp();

    @Nullable
    List<ChapterDTO> getChapters();

}
