package x.xmlish;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootApplication
public class XmlishApplication {
    public static void main(final String... args) {
        SpringApplication.run(XmlishApplication.class, args);
    }
}
