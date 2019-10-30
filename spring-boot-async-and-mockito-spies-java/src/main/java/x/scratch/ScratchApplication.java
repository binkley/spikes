package x.scratch;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static x.scratch.AsyncConfiguration.SLOW_EXECUTOR_BEAN_NAME;

@SpringBootApplication
public class ScratchApplication {
    public static void main(final String[] args) {
        SpringApplication.run(ScratchApplication.class, args);
    }

    @Component
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    public static class Sally {
        private final Bob bob;

        public void runItEventuallyButQuickly() {
            bob.runItEventuallyButQuickly();
        }

        public void runItEventuallyButSlowly() {
            bob.runItEventuallyButSlowly();
        }
    }

    @Component
    public static class Bob {
        @Async
        public void runItEventuallyButQuickly() {}

        @Async(SLOW_EXECUTOR_BEAN_NAME)
        public void runItEventuallyButSlowly() {}
    }
}
