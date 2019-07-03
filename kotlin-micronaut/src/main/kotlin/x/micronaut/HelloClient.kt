package x.micronaut

import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.reactivex.Single

@Client("/hello")
interface HelloClient {
    @Post
    fun greet(request: HelloRequest): Single<HelloResponse>
}
