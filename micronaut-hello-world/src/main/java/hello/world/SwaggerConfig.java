package hello.world;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import java.util.List;

@ConfigurationProperties("swagger")
@Data
@Introspected
public class SwaggerConfig {
    private String version;
    private String layout;
    private boolean deepLinking;
    private boolean displayRequestDuration;
    private boolean showCommonExtensions;
    private boolean showExtensions;
    private List<URIConfig> urls;

    @ConfigurationProperties("urls")
    @Data
    public static class URIConfig {
        private String name;
        private String url;
    }
}
