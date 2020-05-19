package x.scratch

fun main() {
    println("== ${Fib0.fib}")
    println("1/F0 -> ${-Fib0}")
    println("F0^2 -> ${Fib0.pow(2)}")
    println("F0^-2 -> ${Fib0.pow(-2)}")
    println("== ${Fib1.fib}")
    println("1/F1 -> ${-Fib1}")
    println("F1^2 -> ${Fib1.pow(2)}")
    println("F1^-2 -> ${Fib1.pow(-2)}")
    println("== ${(-Fib1).fib}")
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
}

val Fib.fib get() = b
val Fib.det get() = if (0 == n % 2) -1 else 1

private fun fib(n: Int) = when {
    0 == n -> Fib0
    0 < n -> posFib(n, Fib1)
    else -> negFib(n, FibM1)
}

private fun posFib(n: Int, base: Fib): Fib {
    var nn = n
    var fib = Fib0
    while (nn > 0) {
        --nn
        fib = Fib(
            n,
            fib.a * base.a + fib.b * base.c,
            fib.a * base.b + fib.b * base.d,
            fib.c * base.a + fib.d * base.c,
            fib.c * base.b + fib.d * base.d
        )
    }
    return fib
}

private fun negFib(n: Int, base: Fib): Fib {
    var nn = -n
    var fib = Fib0
    while (nn > 0) {
        --nn
        fib = Fib(
            n,
            fib.a * base.a + fib.b * base.c,
            fib.a * base.b + fib.b * base.d,
            fib.c * base.a + fib.d * base.c,
            fib.c * base.b + fib.d * base.d
        )
    }
    return fib
}

operator fun Fib.unaryPlus() = this
operator fun Fib.unaryMinus() = fib(-n)

fun Fib.pow(p: Int) = fib(n * p)

operator fun Fib.plus(multiplicand: Fib) = fib(n + multiplicand.n)
operator fun Fib.minus(divisor: Fib) = fib(n - divisor.n)
