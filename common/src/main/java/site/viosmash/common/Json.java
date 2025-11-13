package site.viosmash.common;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;

public class Json {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .registerModule(new JavaTimeModule())  // Register JavaTimeModule for Java 8 date/time types
            .findAndRegisterModules();  // Optionally auto-discover other modules like `jackson-datatype-jsr310`


    public static String to(Object o) {
        try { return MAPPER.writeValueAsString(o); } catch (Exception e) { throw new RuntimeException(e); }
    }
    public static <T> T from(String s, Class<T> t) {
        try { return MAPPER.readValue(s, t); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static <T> T from(String s, TypeReference<T> clzz) {
        try { return MAPPER.readValue(s, clzz); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static ObjectMapper mapper() { return MAPPER; }
}