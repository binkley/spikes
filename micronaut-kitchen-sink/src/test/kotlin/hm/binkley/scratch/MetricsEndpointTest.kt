package hm.binkley.scratch

import io.kotlintest.specs.StringSpec
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
internal class MetricsEndpointTest(
    private val jvmMemoryUsed: MetricsJvmMemoryUsedEndpointClient
) : StringSpec({
    "should provide metrics for JVM memory used" {
        jvmMemoryUsed.get() // If throws, test fails
    }
})
