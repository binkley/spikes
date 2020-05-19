package x.scratch

import x.scratch.Fib.Companion.fib
import kotlin.math.absoluteValue

fun main() {
    val fib0 = fib(0)
    val fib1 = fib(1)

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
}

data class Fib(
    val n: Int,
    val a: Int,
    val b: Int,
    val c: Int,
    val d: Int
) {
    init {
        assert(d == a + b)
    }

    override fun toString() = "F($n)[$a, $b; $c, $d]"

    companion object {
        private val Fib0 = Fib(0, 1, 0, 0, 1)
        private val Fib1 = Fib(1, 0, 1, 1, 1)
        private val FibM1 = Fib(-1, -1, 1, 1, 0)

        fun fib(n: Int) =
            fib0(n, if (n < 0) FibM1 else Fib1, n.absoluteValue, Fib0)
    }
}

val Fib.fib get() = b
val Fib.det get() = if (0 == n % 2) 1 else -1

fun Fib.inv() = fib(-n)
fun Fib.pow(p: Int) = fib(n * p)

operator fun Fib.times(multiplicand: Fib) = fib(n + multiplicand.n)
operator fun Fib.div(divisor: Fib) = fib(n - divisor.n)

private tailrec fun fib0(
    n: Int,
    multiplicand: Fib,
    i: Int,
    fib_i: Fib
): Fib {
    return if (0 == i) fib_i
    else fib0(
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
}
