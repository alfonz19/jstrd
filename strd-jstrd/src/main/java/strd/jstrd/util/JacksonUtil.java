package strd.jstrd.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.exception.JstrdException;
import strd.lib.common.exception.CannotHappenException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class JacksonUtil {
    public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper result = new ObjectMapper();
        result.registerModule(new JavaTimeModule().addSerializer(OffsetDateTime.class, new JsonSerializer<>() {
            @Override
            public void serialize(OffsetDateTime offsetDateTime,
                                  JsonGenerator jsonGenerator,
                                  SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime));
            }
        } ));
        return result;
    }

    public static String serializeAsString(Object result) {
        try {
            return OBJECT_MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new CannotHappenException(e);
        }
    }

    public static StreamDeckConfiguration deserializeJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, StreamDeckConfiguration.class);
        } catch (IOException e) {
            throw new JstrdException("Unable to deserialize streamdeck configuration data", e);
        }
    }
}
