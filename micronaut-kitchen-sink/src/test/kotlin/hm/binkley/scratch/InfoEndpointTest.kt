package hm.binkley.scratch

import io.kotlintest.specs.StringSpec
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest

data class InfoJson(
    val git: GitJson
) {
    data class GitJson(
        val branch: String,
        val build: BuildJson,
        val commit: CommitJson,
        val dirty: Boolean,
        val remote: RemoteJson,
        val total: TotalJson
    ) {
        data class BuildJson(
            val host: String,
            val user: UserJson,
            val version: String
        )

        data class CommitJson(
            val id: String,
            val message: MessageJson,
            // TODO: 2020-01-12T08:48:42-0600 does not parse as an
            //  OffsetDateTime because of -0600 vs -06:00
            val time: String,
            val user: UserJson
        ) {
            data class MessageJson(
                val full: String,
                val short: String
            )
        }

        data class RemoteJson(
            val origin: OriginJson
        ) {
            data class OriginJson(
                val url: String
            )
        }

        data class TotalJson(
            val commit: CommitJson
        ) {
            data class CommitJson(
                val count: Int
            )
        }

        data class UserJson(
            val email: String,
            val name: String
        )
    }
}

@Client("/")
interface InfoEndpointClient {
    @Get("/info")
    fun get(): InfoJson
}

@MicronautTest
class InfoTest(
    private val client: InfoEndpointClient
) : StringSpec({
    "should have JSON build info" {
        client.get() // If throws, test fails
    }
})
