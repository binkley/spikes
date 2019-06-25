package hello.world;

import hello.world.AdminClient.Health;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static io.micronaut.health.HealthStatus.UP;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest()
class AdminControllerSpec {
    @Inject
    private EmbeddedServer server;
    @Inject
    private AdminClient helloClient;

    @Test
    void shouldBeHealthy() {
        final var response = helloClient.health().blockingGet();

        final var expected = new Health();
        expected.setStatus(UP);

        assertEquals(expected, response);
    }
}
