package site.viosmash.common;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Json {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    public static String to(Object o) {
        try { return MAPPER.writeValueAsString(o); } catch (Exception e) { throw new RuntimeException(e); }
    }
    public static <T> T from(String s, Class<T> t) {
        try { return MAPPER.readValue(s, t); } catch (Exception e) { throw new RuntimeException(e); }
    }
    public static ObjectMapper mapper() { return MAPPER; }
}