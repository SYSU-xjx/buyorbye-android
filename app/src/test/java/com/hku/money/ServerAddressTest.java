package com.hku.money;

import com.hku.money.data.ServerAddress;
import org.junit.Test;
import static org.junit.Assert.*;

public class ServerAddressTest {
    @Test public void acceptsOriginAndApiBase() {
        assertEquals("http://10.0.2.2:8080/api/", ServerAddress.normalize("http://10.0.2.2:8080"));
        assertEquals("https://example.com/service/api/", ServerAddress.normalize("https://example.com/service/api/"));
    }

    @Test public void acceptsSwaggerWithContextPath() {
        assertEquals("https://example.com/service/api/",
                ServerAddress.normalize("https://example.com/service/swagger-ui/index.html"));
    }

    @Test public void rejectsCredentialsAndAmbiguousUrls() {
        for (String url : new String[]{"localhost:8080", "https://user:pass@example.com",
                "https://example.com/?key=secret", "https://example.com/#token"}) {
            try {
                ServerAddress.normalize(url);
                fail("Should reject " + url);
            } catch (IllegalArgumentException expected) {
                // Prevent credentials or URL query values from becoming a saved API base.
            }
        }
    }
}
