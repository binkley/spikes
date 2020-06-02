package x.scratch

import java.math.BigInteger
import java.util.Objects.hash
import kotlin.math.absoluteValue

fun main() {
    val fib0 = Fib(0)
    val fib1 = Fib(1)

    println("UNIT: $fib0")
    println("GENERATOR: $fib1")
    println()
    println("== SCALAR FIB: ${fib0.fib}; DET: ${fib0.det}")
    println("F0 * F0 -> ${fib0 * fib0}")
    println("F0 / F0 -> ${fib0 / fib0}")
    println("1/F0 -> ${fib0.inv()}")
    println("F0^2 -> ${fib0.pow(2)}")
    println("F0^-2 -> ${fib0.pow(-2)}")
    println("F0√2 -> ${fib0.root(2)}")
    println()
    println("== SCALAR FIB: ${fib1.fib}; DET ${fib1.det}")
    println("F1 * F1 -> ${fib1 * fib1}")
    println("F1 / F1 -> ${fib1 / fib1}")
    println("1/F1 -> ${fib1.inv()}")
    println("F1^2 -> ${fib1.pow(2)}")
    println("F1^-2 -> ${fib1.pow(-2)}")
    println()
    println("== SCALAR INV FIB: ${fib1.inv().fib}; DET ${fib1.inv().det}")
    println("1/F1 * 1/F1 -> ${fib1.inv() * fib1.inv()}")
    println("1/F1 / 1/F1 -> ${fib1.inv() / fib1.inv()}")
    println("1/(1/F1) -> ${fib1.inv().inv()}")
    println("(1/F1)^2 -> ${fib1.inv().pow(2)}")
    println("(1/F1)^-2 -> ${fib1.inv().pow(-2)}")
    println()
    println("== BIG DESTRUCTURED")
    val (n, a, b, c, d) = Fib(100)
    println("f$n -> $b OR $c; PRED: $a; SUCC: $d")
    println()
    println("== SEQUENCE")
    for (n in -3..0) {
        val fib = Fib(n)
        println(
            "Fib($n) -> $fib; Fib($n)^1 -> ${fib.inv()}; |Fib($n)| -> ${fib.absoluteValue}"
        )
    }
    for (n in 1..3) {
        val fib = Fib(n)
        println(
            "Fib($n) -> $fib; Fib($n)^1 -> ${fib.inv()}; |Fib($n)| -> ${fib.absoluteValue}; $n√ -> ${fib.root(
                n.absoluteValue
            )}"
        )
    }
}

// Why not a data class?  Avoid the `copy` function, which would permit
// construction of invalid Fib matrices.
class Fib internal constructor(
    val n: Int,
    val a: BigInteger,
    val b: BigInteger,
    val c: BigInteger,
    val d: BigInteger
) {
    override fun equals(other: Any?) = this === other ||
            other is Fib &&
            n == other.n

    override fun hashCode() = hash(javaClass, n)
    override fun toString() = "F($n)[$a, $b; $c, $d]"
}

/**
 * Creates the [n]th 2x2 Fibonacci matrix.  The matrix is of the form:
 * `[fib(n-1), fib(n); fib(n), fib(n+1)]`.
 *
 * `Fib(0)` is `[1, 0; 0, 1]` (the unit matrix).  `Fib(1)` is
 * `[0, 1; 1, 1]`, (the generator matrix).
 *
 * Successive Fibonacci matrices are given by _multiplying_ by the generator
 * matrix for next, and _dividing_ for previous.
 */
fun Fib(n: Int): Fib = when {
    0 == n -> UNIT
    1 == n -> GENERATOR
    0 < n -> fibN(n, n, UNIT)
    else -> Fib(-n).inv()
}

val Fib.fib get() = b

/**
 * One of the nifty properties of the Fibonacci generator matrix is that the
 * determinant alternates between 1 and -1, starting at 1 for the UNIT fib.
 */
val Fib.det get() = if (0 == n % 2) 1 else -1
val Fib.absoluteValue get() = if (0 > n) inv() else this

operator fun Fib.component1() = n
operator fun Fib.component2() = a
operator fun Fib.component3() = b
operator fun Fib.component4() = c
operator fun Fib.component5() = d

/** The product of `Fib(m)` and `Fib(n)` is `Fib(m+n)`. */
operator fun Fib.times(multiplicand: Fib) = Fib(n + multiplicand.n)

/** The quotient of `Fib(m)` and `Fib(n)` is `Fib(m-n)` */
operator fun Fib.div(divisor: Fib) = Fib(n - divisor.n)

/**
 * The multiplicative inverse of a Fibonacci matrix is found easily by:
 * 1. Transpose along the minor diagonal (antidiagonal)
 * 2. Flip the signs of the main diagonal
 */
fun Fib.inv() = when {
    0 == n -> this
    0 > n -> Fib(-n, d.abs(), b.abs(), c.abs(), a.abs())
    0 == n % 2 -> Fib(-n, d, -b, -c, a)
    else -> Fib(-n, -d, b, c, -a)
}

fun Fib.pow(p: Int) = Fib(n * p)
fun Fib.root(p: Int) =
    when {
        0 > n -> throw ArithmeticException("Fib may not be complex")
        0 == p -> throw ArithmeticException("Division by zero")
        1 == p -> this
        0 == n % p -> Fib(n / p)
        else -> throw ArithmeticException("Fib may not be fractional")
    }

private val UNIT = Fib(0, 1.big, 0.big, 0.big, 1.big)
private val GENERATOR = Fib(1, 0.big, 1.big, 1.big, 1.big)

/**
 * @todo Note articles like
 *   <a href="https://dzone.com/articles/avoid-recursion"><cite>Avoid Recursion in ConcurrentHashMap.computeIfAbsent()</cite></a>,
 *   which remains true even now
 * @todo Replace with divide-and-conquer algo using memoization
 */
private tailrec fun fibN(
    n: Int,
    i: Int,
    fib_i: Fib
): Fib {
    return if (0 == i) fib_i
    else fibN(
        n,
        i - 1,
        multiply0(n, fib_i)
    )
}

private fun multiply0(n: Int, left: Fib) = Fib(
    n,
    left.a * GENERATOR.a + left.b * GENERATOR.c,
    left.a * GENERATOR.b + left.b * GENERATOR.d,
    left.c * GENERATOR.a + left.d * GENERATOR.c,
    left.c * GENERATOR.b + left.d * GENERATOR.d
)

private val Int.big get() = toBigInteger()
