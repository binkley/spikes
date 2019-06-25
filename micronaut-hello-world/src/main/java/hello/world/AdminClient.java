package hello.world;

import io.micronaut.health.HealthStatus;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.Single;
import lombok.Data;

@Client("/")
public interface AdminClient {
    @Get("health")
    Single<Health> health();

    @Data
    class Health {
        // {"status":"UP"}
        private HealthStatus status;
    }
}
