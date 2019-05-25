package x.xmlish;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.StreamUtils.copyToString;

public class ReadXml {
    private static final ResourceLoader resourceLoader
            = new DefaultResourceLoader();

    public static String readXml(final String name) {
        try {
            return copyToString(resourceLoader
                    .getResource("xml/" + name + ".xml")
                    .getInputStream(), UTF_8);
        } catch (final IOException e) {
            throw new Bug(e);
        }
    }
}
