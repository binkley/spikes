package x.scratch

import kotlin.reflect.typeOf

@UseExperimental(ExperimentalStdlibApi::class)
fun main() {
    val intType = typeOf<Int>()
    println(intType)

    accessReifiedTypeArg<String>()
    accessReifiedTypeArg<List<String>>()

    val first = MyClass(1)
    val second = MyClass(1)
    println(first == second)
}

@UseExperimental(ExperimentalStdlibApi::class)
inline fun <reified T> accessReifiedTypeArg() {
    val kType = typeOf<T>()
    println(kType.toString())
}

inline class MyClass(val value: Int)
