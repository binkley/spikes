package x.validating;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
public class ValidatingApplication {
    public static void main(final String... args) {
        SpringApplication.run(ValidatingApplication.class, args);
    }
}
