package x.scratch

import x.scratch.Fib.Companion.fib
import kotlin.math.absoluteValue

fun main() {
    println("== ${Fib0.fib} det ${Fib0.det}")
    println("1/F0 -> ${-Fib0}")
    println("F0^2 -> ${Fib0.pow(2)}")
    println("F0^-2 -> ${Fib0.pow(-2)}")
    println("== ${Fib1.fib} det ${Fib1.det}")
    println("1/F1 -> ${-Fib1}")
    println("F1^2 -> ${Fib1.pow(2)}")
    println("F1^-2 -> ${Fib1.pow(-2)}")
    println("== ${(-Fib1).fib} det ${(-Fib1).det}")
    println("1/(1/F1) -> ${-(-Fib1)}")
    println("(1/F1)^2 -> ${(-Fib1).pow(2)}")
    println("(1/F1)^-2 -> ${(-Fib1).pow(-2)}")
}

private val Fib0 = Fib(0, 1, 0, 0, 1)
private val Fib1 = Fib(1, 0, 1, 1, 1)
private val FibM1 = Fib(-1, -1, 1, 1, 0)

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
        fun fib(n: Int): Fib {
            val multiplicand = if (n < 0) FibM1 else Fib1
            var fib = Fib0
            var nn = n.absoluteValue
            while (nn > 0) {
                --nn
                fib = Fib(
                    n,
                    fib.a * multiplicand.a + fib.b * multiplicand.c,
                    fib.a * multiplicand.b + fib.b * multiplicand.d,
                    fib.c * multiplicand.a + fib.d * multiplicand.c,
                    fib.c * multiplicand.b + fib.d * multiplicand.d
                )
            }
            return fib
        }
    }
}

val Fib.fib get() = b
val Fib.det get() = if (0 == n % 2) 1 else -1

operator fun Fib.unaryPlus() = this
operator fun Fib.unaryMinus() = fib(-n)

fun Fib.pow(p: Int) = fib(n * p)

operator fun Fib.plus(multiplicand: Fib) = fib(n + multiplicand.n)
operator fun Fib.minus(divisor: Fib) = fib(n - divisor.n)
