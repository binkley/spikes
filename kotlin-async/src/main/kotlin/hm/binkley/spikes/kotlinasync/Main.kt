package hm.binkley.spikes.kotlinasync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
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

        squares().consumeEach { println(it) }
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

@ExperimentalCoroutinesApi
fun CoroutineScope.squares(): ReceiveChannel<Int> = produce(capacity = 5) {
    for (x in 1..5) send(x * x)
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
