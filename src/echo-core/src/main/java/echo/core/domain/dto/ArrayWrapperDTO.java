package echo.core.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

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
@JsonSerialize(as = ImmutableArrayWrapperDTO.class)
@JsonDeserialize(as = ImmutableArrayWrapperDTO.class)
public interface ArrayWrapperDTO<T> {

    @Value.Parameter
    List<T> getResults();

}
