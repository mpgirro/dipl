package echo.core.domain.dto.immutable;

import echo.core.domain.dto.EntityDTO;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Maximilian Irro
 */
@Value.Immutable
@Value.Modifiable            // generates implementation with setters, required by mappers
@Value.Style(
    get        = {"is*", "get*"}, // Detect 'get' and 'is' prefixes in accessor methods
    init       = "set*",
    create     = "new",// generates public no args constructor
    //builder = "new", // construct builder using 'new' instead of factory method
    build      = "create", // rename 'build' method on builder to 'create'
    visibility = Value.Style.ImplementationVisibility.PUBLIC, // Generated class will be always public
    defaults   = @Value.Immutable(copy = false)) // Disable copy methods by default
public interface TestEpisode {

    @Nullable Long getId();
    @Nullable Long getPodcastId();
    @Nullable String getEchoId();
    @Nullable String getPodcastEchoId();
    @Nullable String getPodcastTitle();
    @Nullable String getTitle();
    @Nullable String getLink();
    @Nullable LocalDateTime getPubDate();
    @Nullable String getGuid();
    @Nullable Boolean getGuidIsPermaLink();
    @Nullable String getDescription();
    @Nullable String getImage();
    @Nullable String getItunesDuration();
    @Nullable String getItunesSubtitle();
    @Nullable String getItunesAuthor();
    @Nullable String getItunesSummary();
    @Nullable Integer getItunesSeason();
    @Nullable Integer getItunesEpisode();
    @Nullable String getItunesEpisodeType();
    @Nullable String getEnclosureUrl();
    @Nullable Long getEnclosureLength();
    @Nullable String getEnclosureType();
    @Nullable String getContentEncoded();
    @Nullable LocalDateTime getRegistrationTimestamp();
    @Nullable List<TestChapter> getChapters();

}
