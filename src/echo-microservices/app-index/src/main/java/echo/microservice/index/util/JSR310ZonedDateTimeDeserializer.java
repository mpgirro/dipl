package echo.microservice.index.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * Custom Jackson deserializer for transforming a JSON object (using the ISO 8601 date formatwith time)
 * to a JSR310 ZonedDateTime object.
 *
 * @author Maximilian Irro
 */
public class JSR310ZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {

    public static final JSR310ZonedDateTimeDeserializer INSTANCE = new JSR310ZonedDateTimeDeserializer();

    private JSR310ZonedDateTimeDeserializer() {}

    private static final DateTimeFormatter ISO_DATE_TIME;
    private static final DateTimeFormatter ISO_OFFSET_DATE_TIME;

    static {
        ISO_DATE_TIME = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .toFormatter();
        ISO_OFFSET_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    }

    @Override
    public ZonedDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        switch(parser.getCurrentToken()) {
            case START_ARRAY:
                if(parser.nextToken() == JsonToken.END_ARRAY) {
                    return null;
                }
                int year = parser.getIntValue();

                parser.nextToken();
                int month = parser.getIntValue();

                parser.nextToken();
                int day = parser.getIntValue();

                parser.nextToken();
                int hour = parser.getIntValue();

                parser.nextToken();
                int min = parser.getIntValue();

                parser.nextToken();
                int sec = parser.getIntValue();

                parser.nextToken();
                int nanosec = parser.getIntValue();

                if(parser.nextToken() != JsonToken.END_ARRAY) {
                    throw context.wrongTokenException(parser, JsonToken.END_ARRAY, "Expected array to end.");
                }
                return ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, min, sec, nanosec), ZoneId.systemDefault());

            case VALUE_STRING:
                String string = parser.getText().trim();
                if(string.length() == 0) {
                    return null;
                } else if(string.length() > 19){
                    LocalDateTime dateTime = LocalDateTime.parse(string, ISO_OFFSET_DATE_TIME);
                    return ZonedDateTime.ofInstant(dateTime, ZoneOffset.UTC, ZoneId.systemDefault());
                } else {
                    LocalDateTime dateTime = LocalDateTime.parse(string, ISO_DATE_TIME);
                    return ZonedDateTime.ofInstant(dateTime, ZoneOffset.UTC, ZoneId.systemDefault());
                }

        }
        throw context.wrongTokenException(parser, JsonToken.START_ARRAY, "Expected array or string.");
    }
}
