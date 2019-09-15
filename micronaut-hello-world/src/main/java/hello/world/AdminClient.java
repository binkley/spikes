package hello.world;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import io.micronaut.health.HealthStatus;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.Single;
import lombok.Data;

import java.io.IOException;

import static io.micronaut.health.HealthStatus.DOWN;
import static io.micronaut.health.HealthStatus.NAME_DOWN;
import static io.micronaut.health.HealthStatus.NAME_UP;
import static io.micronaut.health.HealthStatus.UP;

@Client("/admin")
public interface AdminClient {
    @Get("health")
    Single<Health> health();

    @Data
    class Health {
        // {"status":"UP"}
        @JsonDeserialize(using = HealthDeserialize.class)
        private HealthStatus status;
    }

    /**
     * Issues: <ol>
     * <li>Why is this class needed?  1.2.0.RC1 did not need this</li>
     * <li>This works <strong>only</strong> for simple health statuses (ie,
     * no description, etc)</li>
     * </ol>
     */
    class HealthDeserialize
            extends StdScalarDeserializer<HealthStatus> {
        public HealthDeserialize() {
            super(HealthStatus.class);
        }

        @Override
        public HealthStatus deserialize(final JsonParser jp,
                final DeserializationContext ctxt)
                throws IOException {
            final var name = _parseString(jp, ctxt);
            switch (name) {
            case NAME_UP:
                return UP;
            case NAME_DOWN:
                return DOWN;
            default:
                throw new UnsupportedOperationException(
                        "BUG: Borken code: " + name);
            }
        }
    }
}
