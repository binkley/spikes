package hello.world;

import hello.world.SwaggerConfig.URIConfig;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;
import io.micronaut.views.View;
import io.swagger.v3.oas.annotations.Hidden;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Controller("/swagger")
@Hidden
@Validated
public class SwaggerController {
    @Inject
    SwaggerConfig config;

    @View("swagger/index")
    @Get
    public SwaggerConfig index() {
        return config;
    }

    @View("swagger/index")
    @Get("/{url}")
    public SwaggerConfig renderSpec(final @NotNull String url) {
        final var config = new SwaggerConfig();
        config.setDeepLinking(this.config.isDeepLinking());
        config.setLayout(this.config.getLayout());
        final var uriConfig = new URIConfig();
        uriConfig.setName(url);
        uriConfig.setUrl(url);
        config.setUrls(List.of(uriConfig));
        return config;
    }

    @View("swagger/index")
    @Post
    public SwaggerConfig renderSpecs(
            @Body final @NotEmpty List<URIConfig> urls) {
        final var config = new SwaggerConfig();
        config.setDeepLinking(this.config.isDeepLinking());
        config.setLayout(this.config.getLayout());
        config.setUrls(urls);
        return config;
    }
}
