package hm.binkley.scratch

import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.PropertySource
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
@PropertySource(
    Property(name = "tracing.jaeger.enabled", value = "true"),
    Property(name = "tracing.jaeger.sampler.probability", value = "1")
)
internal class FooControllerTest(
    private val foo: FooClient,
    private val listener: TestAuditListener
) : StringSpec({
    "should foo" {
        val json = foo.get()

        json.name shouldBe "Brian"
        json.number shouldBe 42

        listener.events shouldHaveSize 1
    }

    "should name" {
        val json = foo.name("bob")

        json.name shouldBe "bob"

        listener.events.shouldBeEmpty()
    }

    "should NOT name" {
        shouldThrow<HttpClientResponseException> {
            foo.name("BOB")
        }
    }
})

@Client("/foo")
interface FooClient {
    @Get
    fun get(): FooJson

    @Get("/{name}")
    fun name(@PathVariable name: String): NameJson
}
