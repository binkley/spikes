package hm.binkley.scratch

import io.kotlintest.specs.StringSpec
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
@Property(name = "endpoints.info.enabled", value = "true")
internal class InfoEndpointTest(
    private val info: InfoEndpointClient
) : StringSpec({
    "should have build info as JSON" {
        info.get() // If throws, test fails
    }
})
