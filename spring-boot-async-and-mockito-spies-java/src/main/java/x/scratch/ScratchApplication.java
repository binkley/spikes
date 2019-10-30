package x.scratch;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@EnableAsync
@SpringBootApplication
public class ScratchApplication {
    public static void main(final String[] args) {
        SpringApplication.run(ScratchApplication.class, args);
    }

    @Component
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    public static class Sally {
        private final Bob bob;

        public void runIt() {
            bob.runItEventually();
        }
    }

    @Component
    public static class Bob {
        @Async
        public void runItEventually() {}
    }
}
