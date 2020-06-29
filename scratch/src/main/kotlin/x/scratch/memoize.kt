package x.scratch

import x.scratch.Memoize.Companion.memoize
import x.scratch.MemoizedFactorial.Companion.factorial
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

private class MemoizedFactorial : (Long, Long) -> Long {
    /**
     * Track the memoized function so that invoke can call back to it without
     * re-memoizing.
     */
    private val mF = this::invoke.memoize()

    /** Not the Gamma or Pi functions. */
    override fun invoke(n: Long, a: Long): Long = when (n) {
        1L -> 1L
        else -> {
            if (NOISY) println("... memoizing $n with $a so far")
            // Careful here _not_ to call ourselves, but the memoized function
            n * mF(n - 1, n * a)
        }
    }

    companion object {
        private val cached = MemoizedFactorial()

        /**
         * Quirky, but use the memoized function for the first call, not a
         * call to invoke, so that even the first call can skip computation.
         */
        fun factorial(n: Long): Long = cached.mF(n, 1)
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
