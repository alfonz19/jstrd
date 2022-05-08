package strd.jstrd.util;

import strd.jstrd.exception.JstrdException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

public class SerializableUtil {
    //hide me!
    private SerializableUtil() {}

    public static String serializableToBase64(Serializable serializable) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(serializable);
            so.flush();

            return Base64.getEncoder().encodeToString(bo.toByteArray());
        } catch (Exception e) {
            throw new JstrdException("Failed to serialize serializable instance to base64", e);
        }
    }

    public static <T> T deserializableFromBase64(String message, Class<T> clazz) {
        return clazz.cast(deserializableFromBase64(message));
    }

    public static Serializable deserializableFromBase64(String message) {
        byte[] bytes = Base64.getDecoder().decode(message);
        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream si = new ObjectInputStream(bi);
            return (Serializable) si.readObject();
        } catch (Exception e) {
            throw new JstrdException("Failed to deserialize serializable instance from base64", e);
        }
    }
}
