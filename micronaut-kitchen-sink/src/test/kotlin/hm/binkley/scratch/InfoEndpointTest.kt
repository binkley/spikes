package hm.binkley.scratch

import io.kotlintest.specs.StringSpec
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
internal class InfoEndpointTest(
    private val info: InfoEndpointClient
) : StringSpec({
    "should have build info as JSON" {
        info.get() // If throws, test fails
    }
})
