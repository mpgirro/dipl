package echo.core.util;

import echo.core.mapper.DateMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author Maximilian Irro
 */
public final class JSR310PersistenceConverters {

    private JSR310PersistenceConverters() {}

    private static DateMapper dateMapper = DateMapper.INSTANCE;

    @Converter(autoApply = true)
    public static class LocalDateConverter implements AttributeConverter<LocalDate, java.sql.Date> {

        @Override
        public java.sql.Date convertToDatabaseColumn(LocalDate date) {
            return dateMapper.asSqlDate(date);
        }

        @Override
        public LocalDate convertToEntityAttribute(java.sql.Date date) {
            return dateMapper.asLocalDate(date);
        }
    }

    @Converter(autoApply = true)
    public static class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Date> {

        @Override
        public Date convertToDatabaseColumn(ZonedDateTime zonedDateTime) {
            return dateMapper.asDate(zonedDateTime);
        }

        @Override
        public ZonedDateTime convertToEntityAttribute(Date date) {
            return dateMapper.asZonedDateTime(date);
        }
    }

    @Converter(autoApply = true)
    public static class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Date> {

        @Override
        public Date convertToDatabaseColumn(LocalDateTime localDateTime) {
            return dateMapper.asDate(localDateTime);
        }

        @Override
        public LocalDateTime convertToEntityAttribute(Date date) {
            return dateMapper.asLocalDateTime(date);
        }
    }
}
