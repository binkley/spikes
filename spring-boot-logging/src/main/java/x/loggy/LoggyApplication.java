package x.loggy;

import joptsimple.OptionParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.io.IOException;

import static java.lang.System.exit;
import static java.lang.System.out;
import static org.springframework.boot.SpringApplication.run;

@EnableFeignClients
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootApplication
public class LoggyApplication {
    public static void main(final String... args)
            throws IOException {
        final var optionParser = new OptionParser();
        final var help = optionParser.accepts("help",
                "Print help and exit")
                .forHelp();
        final var keepGoing = optionParser.accepts("continue",
                "Continue running the application after the demo");
        final var noDemo = optionParser.accepts("no-demo",
                "Do not first run the demo; implies \"continue\"");
        final var options = optionParser.parse(args);

        if (options.has(help)) {
            //noinspection UseOfSystemOutOrSystemErr
            optionParser.printHelpOn(out);
            exit(0);
        }

        if (options.has(keepGoing) || options.has(noDemo))
            System.setProperty("loggy.run-once", "false");
        if (options.has(noDemo))
            System.setProperty("loggy.enable-demo", "false");

        final var context = run(LoggyApplication.class, args);
        final var loggy = context.getBean(LoggyProperties.class);
        if (loggy.isRunOnce()) {
            context.close();
        }
    }
}
