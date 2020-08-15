package x.scratch

import java.lang.IllegalStateException
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.ZERO

/** See https://youtrack.jetbrains.com/issue/KT-30330 */
fun main(args: Array<String>) {
    println("== NOTHING")
    println("-- Demonstrate new KotlinNothingValueException instead of NPE")
    try {
        val r = "str".decodeOut(Context<Any>())
        throw IllegalStateException("SHOULD THROW BY DID NOT: $r")
    } catch (e: NullPointerException) {
        println("WRONG EXCEPTION: $e")
        e.printStackTrace()
    } catch (e: RuntimeException) {
        // Tricksy, KNVE is internal
        if (e.javaClass.simpleName != "KotlinNothingValueException")
            throw IllegalStateException("WRONG EXCEPTION: $e")
        println("RIGHT EXCEPTION: $e")
        e.printStackTrace()
    }
}

private fun <T> something(): T = Any() as T

private class Context<T>

private fun <T> Any.decodeIn(typeFrom: Context<in T>): T = something()

private fun <T> Any?.decodeOut(typeFrom: Context<out T>): T {
    return this?.decodeIn(typeFrom) ?: error(
        "")  // decodeIn result is of type Nothing
}
