package echo.core.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import echo.core.domain.feed.FeedStatus;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

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
@JsonSerialize(as = ImmutableFeedDTO.class)
@JsonDeserialize(as = ImmutableFeedDTO.class)
public interface FeedDTO {

    @Nullable
    Long getId();

    @Nullable
    Long getPodcastId();

    @Nullable
    String getExo();

    @Nullable
    String getPodcastExo();

    @Nullable
    String getUrl();

    @Nullable
    LocalDateTime getLastChecked();

    @Nullable
    FeedStatus getLastStatus();

    @Nullable
    LocalDateTime getRegistrationTimestamp();

}
