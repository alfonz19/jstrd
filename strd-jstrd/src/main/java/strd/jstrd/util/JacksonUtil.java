package strd.jstrd.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import strd.lib.common.exception.CannotHappenException;

public class JacksonUtil {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String serializeAsString(Object result) {
        try {
            return OBJECT_MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new CannotHappenException(e);
        }
    }
}
