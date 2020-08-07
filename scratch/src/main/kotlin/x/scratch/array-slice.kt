package x.scratch

import java.lang.System.identityHashCode
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.ZERO

fun main() {
    println("==ARRAY SLICE")

    val s0 = "a"
    val s1 = "b"
    val s2 = "c"
    val a = arrayOf(s0, s1, s2)
    val b = a.slice(2..2)

    println("${identityHashCode(a)} -> ${a::class.java} -> $a")
    println("${identityHashCode(b)} -> ${b::class.java} -> $b")
}
