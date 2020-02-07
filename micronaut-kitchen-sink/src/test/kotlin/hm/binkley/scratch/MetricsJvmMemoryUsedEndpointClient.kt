package hm.binkley.scratch

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client

@Client("/admin/metrics/jvm.memory.used")
interface MetricsJvmMemoryUsedEndpointClient {
    @Get
    fun get(): MetricsJvmMemoryUsed
}

data class MetricsJvmMemoryUsed(
    val name: String
)
