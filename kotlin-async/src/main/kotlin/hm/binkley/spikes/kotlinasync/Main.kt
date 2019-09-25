package hm.binkley.spikes.kotlinasync

import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        foo()
    }
}

suspend fun foo() {
    println("Foo!")
}
