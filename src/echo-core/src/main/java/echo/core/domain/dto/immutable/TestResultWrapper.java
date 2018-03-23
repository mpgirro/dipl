package echo.core.domain.dto.immutable;

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
    visibility = Value.Style.ImplementationVisibility.PUBLIC, // Generated class will be always public
    defaults   = @Value.Immutable(copy = false)) // Disable copy methods by default
public interface TestResultWrapper {

    @Nullable Integer currPage();
    @Nullable Integer maxPage();
    @Nullable Integer totalHits();
    @Nullable TestIndexDoc[] results();

}
