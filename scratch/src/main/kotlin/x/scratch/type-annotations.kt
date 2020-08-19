package x.scratch

import kotlin.reflect.full.functions

fun main() {
    println("==TYPE ANNOTATIONS")

    AnExample::class.functions.forEach {
        println("$it -> ${it.returnType.annotations}")
    }
}

@Target(AnnotationTarget.TYPE)
private annotation class Fooby

private class AnExample {
    fun foo(): @Fooby String = "Okey-dokey"
}
