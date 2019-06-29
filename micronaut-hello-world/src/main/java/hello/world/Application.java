package hello.world;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(info = @Info(
        title = "Hello World",
        version = "0.1",
        description = "HI, MOM!",
        license = @License(
                name = "Public Domain",
                url = "http://unlicense.org")))
public class Application {
    public static void main(final String... args) {
        Micronaut.run(Application.class);
    }
}
