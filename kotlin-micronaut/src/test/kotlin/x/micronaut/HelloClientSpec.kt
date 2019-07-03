package x.micronaut

import io.micronaut.test.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class HelloClientSpec {
    @Inject
    lateinit var client: HelloClient

    @Test
    fun testHelloWorldResponse() {
        assertThat(client.greet(HelloRequest("World")))
                .isEqualTo(HelloResponse("Hello, World!"))
    }
}
