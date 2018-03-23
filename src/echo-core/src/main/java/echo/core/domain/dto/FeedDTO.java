package echo.core.domain.dto;

import echo.core.domain.feed.FeedStatus;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

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
    visibility = Value.Style.ImplementationVisibility.PUBLIC // Generated class will be always public
)
public interface FeedDTO {

    @Nullable Long getId();
    @Nullable Long getPodcastId();
    @Nullable String getEchoId();
    @Nullable String getPodcastEchoId();
    @Nullable String getUrl();
    @Nullable LocalDateTime getLastChecked();
    @Nullable FeedStatus getLastStatus();
    @Nullable LocalDateTime getRegistrationTimestamp();

}
