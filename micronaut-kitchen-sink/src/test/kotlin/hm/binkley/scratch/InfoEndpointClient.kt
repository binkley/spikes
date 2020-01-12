package hm.binkley.scratch

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import java.time.Instant

@Client("/admin/info")
interface InfoEndpointClient {
    @Get
    fun get(): InfoEndpointJson
}

data class InfoEndpointJson(
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
            val time: Instant,
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
