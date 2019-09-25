package hm.binkley.spikes.kotlinasync

import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        foo()
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

class Foo(private var foo: Int) {
    suspend fun getFoo() = foo
    suspend fun setFoo(foo: Int) {
        this.foo = foo
    }
}

suspend operator fun Foo.invoke() = getFoo()
suspend operator fun Foo.invoke(foo: Int) = setFoo(foo)
