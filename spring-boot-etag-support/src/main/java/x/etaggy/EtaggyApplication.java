package x.etaggy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootApplication
public class EtaggyApplication {
    public static void main(final String... args) {
        SpringApplication.run(EtaggyApplication.class, args);
    }
}
