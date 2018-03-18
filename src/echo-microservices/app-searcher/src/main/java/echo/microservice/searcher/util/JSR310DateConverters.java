package echo.microservice.searcher.util;

import echo.core.mapper.DateMapper;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author Maximilian Irro
 */
public final class JSR310DateConverters {

    private static DateMapper dateMapper = DateMapper.INSTANCE;

    private JSR310DateConverters() {}

    public static class LocalDateToDateConverter implements Converter<LocalDate, Date> {

        public static final LocalDateToDateConverter INSTANCE = new LocalDateToDateConverter();

        private LocalDateToDateConverter() {}

        @Override
        public Date convert(LocalDate source) {
            return dateMapper.asDate(source);
        }
    }

    public static class DateToLocalDateConverter implements Converter<Date, LocalDate> {
        public static final DateToLocalDateConverter INSTANCE = new DateToLocalDateConverter();
        private DateToLocalDateConverter() {}

        @Override
        public LocalDate convert(Date source) {
            return dateMapper.asLocalDate(source);
        }
    }

    public static class ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {
        public static final ZonedDateTimeToDateConverter INSTANCE = new ZonedDateTimeToDateConverter();
        private ZonedDateTimeToDateConverter() {}

        @Override
        public Date convert(ZonedDateTime source) {
            return dateMapper.asDate(source);
        }
    }

    public static class DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {
        public static final DateToZonedDateTimeConverter INSTANCE = new DateToZonedDateTimeConverter();
        private DateToZonedDateTimeConverter() {}

        @Override
        public ZonedDateTime convert(Date source) {
            return dateMapper.asZonedDateTime(source);
        }
    }

    public static class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
        public static final LocalDateTimeToDateConverter INSTANCE = new LocalDateTimeToDateConverter();
        private LocalDateTimeToDateConverter() {}

        @Override
        public Date convert(LocalDateTime source) {
            return dateMapper.asDate(source);
        }
    }

    public static class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
        public static final DateToLocalDateTimeConverter INSTANCE = new DateToLocalDateTimeConverter();
        private DateToLocalDateTimeConverter() {}

        @Override
        public LocalDateTime convert(Date source) {
            return dateMapper.asLocalDateTime(source);
        }
    }
}
