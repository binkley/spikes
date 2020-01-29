package hm.binkley.spikes.kotlinasync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    foo()

    val channel = Channel<Int>()
    launch {
        qux(channel)
    }
    launch {
        quux(channel)
    }

    squares().consumeEach { println(it) }

    for (x in squares()) println(x)

    val numbers = produceNumbers()
    val squares = square(numbers)
    repeat(5) {
        println(squares.receive())
    }

    var cur = numbersFrom(2)
    repeat(10) {
        val prime = cur.receive()
        println(prime)
        cur = filter(cur, prime)
    }

    val slowNumbers = slowProduceNumbers()
    repeat(5) {
        launchProcessor(it, slowNumbers)
    }
    delay(950)
    slowNumbers.cancel()

    coroutineContext.cancelChildren()
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

@ExperimentalCoroutinesApi
fun CoroutineScope.produceNumbers() = produce {
    var x = 1
    while (true) send(x++)
}

@ExperimentalCoroutinesApi
fun CoroutineScope.square(numbers: ReceiveChannel<Int>) = produce {
    for (x in numbers) send(x * x)
}

@ExperimentalCoroutinesApi
fun CoroutineScope.numbersFrom(start: Int) = produce {
    var x = start
    while (true) send(x++)
}

@ExperimentalCoroutinesApi
fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) =
    produce {
        for (x in numbers) if (x % prime != 0) send(x)
    }

@ExperimentalCoroutinesApi
fun CoroutineScope.slowProduceNumbers() = produce {
    var x = 1
    while (true) {
        send(x++)
        delay(100)
    }
}

fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) =
    launch {
        for (msg in channel) println("#$id <- $msg")
    }
