package echo.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={UrlMapper.class, DateMapper.class},
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ImmutableMapper {


}
