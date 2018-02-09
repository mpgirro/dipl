package echo.core.converter.mapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

/**
 * @author Maximilian Irro
 */
public class DateMapper {

    public static DateMapper INSTANCE = new DateMapper();

    public String asString(LocalDateTime localDateTime) {
        try {
            return (localDateTime == null ? null : localDateTime.toString());
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

    public LocalDateTime asLocalDateTime(String localDateTime) {
        try {
            return (localDateTime == null ? null : LocalDateTime.parse(localDateTime));
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

    public LocalDateTime asLocalDateTime(Timestamp sqlTimestamp) {
        try {
            return (sqlTimestamp == null ? null : sqlTimestamp.toLocalDateTime());
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

    public ZonedDateTime asZonedDateTime(String zonedDateTime){
        try {
            return (zonedDateTime == null ? null : ZonedDateTime.parse(zonedDateTime));
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

    public Timestamp asTimestamp(LocalDateTime localDateTime){
        try {
            return (localDateTime == null ? null : Timestamp.valueOf(localDateTime));
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

}
