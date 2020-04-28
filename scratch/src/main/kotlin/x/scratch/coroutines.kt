package x.scratch

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val x = launch {
            println("coroutine scope, run blocking: ${this@runBlocking}")
            println(
                "coroutine context, run blocking: ${this@runBlocking.coroutineContext}"
            )
            println("coroutine scope, launch: ${this@launch}")
            println(
                "coroutine context, launch: ${this@launch.coroutineContext}"
            )
            println("Hi!")
        }
        println("x = $x")
        println("x.children = ${x.children.joinToString(", ", "[", "]")}")
        println("x.active = ${x.isActive}")
        println("x.cancelled = ${x.isCancelled}")
        println("x.completed = ${x.isCompleted}")
        println("x.onJoin = ${x.onJoin}")
        x.join()
        println("---")
        println("x = $x")
        println("x.children = ${x.children.joinToString(", ", "[", "]")}")
        println("x.active = ${x.isActive}")
        println("x.cancelled = ${x.isCancelled}")
        println("x.completed = ${x.isCompleted}")
        println("x.onJoin = ${x.onJoin}")
    }
}
