package x.loggy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootApplication
public class LoggyApplication {
    public static void main(final String... args) {
        // FYI -- using the try-block shuts down the program after
        // the command-line runner finishes: Faster feedback cycle
        final var context = SpringApplication.run(
                LoggyApplication.class, args);
        final var loggy = context.getBean(LoggyProperties.class);
        if (loggy.isRunOnce()) {
            context.close();
        }
    }
}
