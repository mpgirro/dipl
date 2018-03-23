package echo.core.domain.dto;

import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.Collections;
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
    build      = "create", // rename 'build' method on builder to 'create'
    visibility = Value.Style.ImplementationVisibility.PUBLIC // Generated class will be always public
)
public abstract class ResultWrapperDTO {

    @Nullable
    public abstract Integer getCurrPage();

    @Nullable
    public abstract Integer getMaxPage();

    @Nullable
    public abstract Integer getTotalHits();

    @Nullable
    public abstract List<IndexDocDTO> getResults();

    public static ResultWrapperDTO empty() {
        return ImmutableResultWrapperDTO.builder()
            .setCurrPage(0)
            .setMaxPage(0)
            .setTotalHits(0)
            .setResults(Collections.emptyList())
            .create();
    }

}
