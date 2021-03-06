package echo.core.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import echo.core.benchmark.rtt.RoundTripTime;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.Collections;
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
@JsonSerialize(as = ImmutableResultWrapperDTO.class)
@JsonDeserialize(as = ImmutableResultWrapperDTO.class)
public interface ResultWrapperDTO {

    @Nullable
    Integer getCurrPage();

    @Nullable
    Integer getMaxPage();

    @Nullable
    Integer getTotalHits();

    @Nullable
    List<IndexDocDTO> getResults();

    @Nullable
    RoundTripTime getRTT();

    static ResultWrapperDTO empty() {
        return ImmutableResultWrapperDTO.builder()
            .setCurrPage(0)
            .setMaxPage(0)
            .setTotalHits(0)
            .setResults(Collections.emptyList()) // this list is immutable
            .setRTT(RoundTripTime.empty())
            .create();
    }

}
