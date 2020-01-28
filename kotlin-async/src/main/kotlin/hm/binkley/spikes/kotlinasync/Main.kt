package hm.binkley.spikes.kotlinasync

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        foo()

        val channel = Channel<Int>()
        launch {
            qux(channel)
        }
        launch {
            quux(channel)
        }
    }
}

class Bar {
    val foo = Foo(3)
}

suspend fun foo() {
    println("Foo!")
    val bar = Bar()
    println(bar.foo())
    bar.foo(2)
    println(bar.foo())
}

suspend fun qux(channel: Channel<Int>) {
    // this might be heavy CPU-consuming computation or async logic, we'll just send five squares
    for (x in 1..5) channel.send(x * x)
    channel.close()
}

suspend fun quux(channel: Channel<Int>) {
    // here we print five received integers:
    repeat(5) { println(channel.receive()) }
    println("Done!")
}

class Foo(private var foo: Int) {
    suspend fun getFoo() = foo
    suspend fun setFoo(foo: Int) {
        this.foo = foo
    }
}

suspend operator fun Foo.invoke() = getFoo()
suspend operator fun Foo.invoke(foo: Int) = setFoo(foo)
