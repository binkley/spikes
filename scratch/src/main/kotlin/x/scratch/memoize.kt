package x.scratch

import x.scratch.Memoize.Companion.memoize
import java.util.concurrent.ConcurrentHashMap

private var NOISY = false

/** See https://jorgecastillo.dev/kotlin-purity-and-function-memoization */
fun main() {
    println("==MEMOIZATION")

    println()
    NOISY = true
    println("MEMOIZED -> 10! -> EXPECT 3628800")
    println(factorial(10))
    println("MEMOIZED -> 5! -> EXPECT 120")
    println(factorial(5))
    println("MEMOIZED -> 6! -> EXPECT 720")
    println(factorial(6))
}

private fun factorial(n: Long): Long = mF(n, 1)

private val mF = ::memoizableFactorial.memoize()

/** Not the Gamma or Pi functions. */
private fun memoizableFactorial(n: Long, a: Long): Long = when (n) {
    1L -> 1L
    else -> {
        if (NOISY) println("... memoizing $n with $a so far")
        n * mF(n - 1, n * a)
    }
}

private class Memoize<in T, in U, out R>(
    private val f: (T, U) -> R
) : (T, U) -> R {
    private val cache = ConcurrentHashMap<T, R>()

    override fun invoke(t: T, u: U): R = cache.getOrPut(t) {
        f(t, u)
    }

    companion object {
        fun <T, U, R> ((T, U) -> R).memoize() = Memoize(this)
    }
}
