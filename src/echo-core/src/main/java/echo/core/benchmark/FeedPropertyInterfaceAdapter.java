package echo.core.benchmark;

import com.google.gson.*;
import echo.core.benchmark.FeedProperty;

import java.lang.reflect.Type;

/**
 * @author Maximilian Irro
 */
final class FeedPropertyInterfaceAdapter<FeedProperty> implements JsonSerializer<FeedProperty>, JsonDeserializer<FeedProperty> {
    @Override
    public FeedProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        return (FeedProperty) ImmutableFeedProperty.builder()
            .setUri(jsonObject.get("uri").getAsString())
            .setLocation(jsonObject.get("location").getAsString())
            .setNumberOfEpisodes(jsonObject.get("numberOfEpisodes").getAsInt())
            .create();
    }

    @Override
    public JsonElement serialize(FeedProperty src, Type typeOfSrc, JsonSerializationContext context) {
        final ImmutableFeedProperty obj = ImmutableFeedProperty.copyOf((echo.core.benchmark.FeedProperty) src);
        final JsonObject wrapper = new JsonObject();
        wrapper.add("uri", context.serialize(obj.getUri()));
        wrapper.add("location", context.serialize(obj.getLocation()));
        wrapper.add("numberOfEpisodes", context.serialize(obj.getNumberOfEpisodes()));
        return wrapper;
    }
}
