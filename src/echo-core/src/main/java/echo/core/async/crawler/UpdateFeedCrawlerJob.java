package echo.core.async.crawler;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import echo.core.benchmark.RoundTripTime;
import org.immutables.value.Value;

/**
 * @author Maximilian Irro
 */
@Value.Immutable
@Value.Style(
    jdkOnly    = true,              // prevent usage of Guava collections
    get        = {"is*", "get*"},   // Detect 'get' and 'is' prefixes in accessor methods
    init       = "set*",
    create     = "new",             // generates public no args constructor
    defaults   = @Value.Immutable(builder = false),  // We may also disable builder
    build      = "create"           // rename 'build' method on builder to 'create'
)
@JsonSerialize(as = ImmutableUpdateFeedCrawlerJob.class)
@JsonDeserialize(as = ImmutableUpdateFeedCrawlerJob.class)
public interface UpdateFeedCrawlerJob extends CrawlerJob {

    @Value.Parameter
    String exo();

    @Value.Parameter
    String url();

    @Value.Parameter
    RoundTripTime getRtt();

}
