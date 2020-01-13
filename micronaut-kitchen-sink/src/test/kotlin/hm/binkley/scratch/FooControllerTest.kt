package hm.binkley.scratch

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.PropertySource
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
@PropertySource(
    Property(name = "tracing.jaeger.enabled", value = "true"),
    Property(name = "tracing.jaeger.sampler.probability", value = "1")
)
internal class FooControllerTest(
    private val foo: FooClient
) : StringSpec({
    "should foo" {
        val json = foo.get()

        json.name shouldBe "Brian"
        json.number shouldBe 42
    }
})

@Client("/foo")
interface FooClient {
    @Get
    fun get(): FooJson
}
