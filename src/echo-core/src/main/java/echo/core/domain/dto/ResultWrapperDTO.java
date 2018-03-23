package echo.core.domain.dto;

import org.immutables.value.Value;

import javax.annotation.Nullable;

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
public abstract class ResultWrapperDTO {

    @Nullable
    public abstract Integer getCurrPage();

    @Nullable
    public abstract Integer getMaxPage();

    @Nullable
    public abstract Integer getTotalHits();

    @Nullable
    public abstract IndexDocDTO[] getResults();

    public static ResultWrapperDTO empty() {
        return ImmutableResultWrapperDTO.builder()
            .setCurrPage(0)
            .setMaxPage(0)
            .setTotalHits(0)
            .setResults(new IndexDocDTO[0])
            .create();
    }

}
