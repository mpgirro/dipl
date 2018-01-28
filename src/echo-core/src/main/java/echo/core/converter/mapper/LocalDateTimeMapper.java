package echo.core.converter.mapper;

import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * @author Maximilian Irro
 */
public class LocalDateTimeMapper {

    public static String asString(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.toString() : null;
    }

    public static LocalDateTime asLocalDateTime(String localDateTime) {
        try {
            return localDateTime!=null ? LocalDateTime.parse(localDateTime) : null;
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

}
