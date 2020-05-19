package x.scratch

import java.math.BigInteger
import java.util.Objects.hash
import kotlin.math.absoluteValue

fun main() {
    val fib0 = Fib(0)
    val fib1 = Fib(1)

    println("== ${fib0.fib} det ${fib0.det}")
    println("F0 * F0 -> ${fib0 * fib0}")
    println("F0 / F0 -> ${fib0 / fib0}")
    println("1/F0 -> ${fib0.inv()}")
    println("F0^2 -> ${fib0.pow(2)}")
    println("F0^-2 -> ${fib0.pow(-2)}")
    println("== ${fib1.fib} det ${fib1.det}")
    println("F1 * F1 -> ${fib1 * fib1}")
    println("F1 / F1 -> ${fib1 / fib1}")
    println("1/F1 -> ${fib1.inv()}")
    println("F1^2 -> ${fib1.pow(2)}")
    println("F1^-2 -> ${fib1.pow(-2)}")
    println("== ${fib1.inv().fib} det ${fib1.inv().det}")
    println("1/F1 * 1/F1 -> ${fib1.inv() * fib1.inv()}")
    println("1/F1 / 1/F1 -> ${fib1.inv() / fib1.inv()}")
    println("1/(1/F1) -> ${fib1.inv().inv()}")
    println("(1/F1)^2 -> ${fib1.inv().pow(2)}")
    println("(1/F1)^-2 -> ${fib1.inv().pow(-2)}")

    println("F100 -> ${Fib(100)}")
    println("F100 -> ${Fib(100)}")
    println(memoized.keys)
}

class Fib internal constructor(
    val n: Int,
    val a: BigInteger,
    val b: BigInteger,
    val c: BigInteger,
    val d: BigInteger
) {
    init {
        assert(d == a + b)
    }

    override fun equals(other: Any?) = this === other ||
            other is Fib &&
            n == other.n

    override fun hashCode() = hash(n)
    override fun toString() = "F($n)[$a, $b; $c, $d]"
}

// TODO: Replace with divide-and-conquer algo using memoization
fun Fib(n: Int) = fib0(n, if (n < 0) Fib_1 else Fib1, n.absoluteValue, Fib0)

val Fib.fib get() = b
val Fib.det get() = if (0 == n % 2) 1 else -1

fun Fib.inv() = Fib(-n)
fun Fib.pow(p: Int) = Fib(n * p)

operator fun Fib.times(multiplicand: Fib) = Fib(n + multiplicand.n)
operator fun Fib.div(divisor: Fib) = Fib(n - divisor.n)

private val Fib_1 = Fib(-1, (-1).big, 1.big, 1.big, 0.big)
private val Fib0 = Fib(0, 1.big, 0.big, 0.big, 1.big)
private val Fib1 = Fib(1, 0.big, 1.big, 1.big, 1.big)

private val memoized = HashMap<Int, Fib>()

/**
 * @todo Note articles like
 *   <a href="https://dzone.com/articles/avoid-recursion"><cite>Avoid Recursion in ConcurrentHashMap.computeIfAbsent()</cite></a>,
 *   which remains true even now
 */
private fun fib0(
    n: Int,
    multiplicand: Fib,
    i: Int,
    fib_i: Fib
): Fib {
    if (0 == i) return fib_i
    if (memoized.contains(n)) return memoized[n]!!

    val fib = fib0(
        n,
        multiplicand,
        i - 1,
        Fib(
            n,
            fib_i.a * multiplicand.a + fib_i.b * multiplicand.c,
            fib_i.a * multiplicand.b + fib_i.b * multiplicand.d,
            fib_i.c * multiplicand.a + fib_i.d * multiplicand.c,
            fib_i.c * multiplicand.b + fib_i.d * multiplicand.d
        )
    )

    memoized[i] = fib
    return fib
}

private val Int.big get() = toBigInteger()
