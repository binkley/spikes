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
    0 < n -> posFib(n)
    else -> negFib(n)
}

private fun posFib(n: Int): Fib {
    var posFib = Fib0
    var nn = n
    while (nn > 0) {
        --nn
        posFib = Fib(
            n,
            posFib.a * Fib1.a + posFib.b * Fib1.c,
            posFib.a * Fib1.b + posFib.b * Fib1.d,
            posFib.c * Fib1.a + posFib.d * Fib1.c,
            posFib.c * Fib1.b + posFib.d * Fib1.d
        )
    }
    return posFib
}

private fun negFib(n: Int): Fib {
    var negFib = Fib0
    var nn = -n
    while (nn > 0) {
        --nn
        negFib = Fib(
            n,
            negFib.a * FibM1.a + negFib.b * FibM1.c,
            negFib.a * FibM1.b + negFib.b * FibM1.d,
            negFib.c * FibM1.a + negFib.d * FibM1.c,
            negFib.c * FibM1.b + negFib.d * FibM1.d
        )
    }
    return negFib
}

operator fun Fib.unaryPlus() = this
operator fun Fib.unaryMinus() = fib(-n)

fun Fib.pow(p: Int) = fib(n * p)

operator fun Fib.plus(multiplicand: Fib) = fib(n + multiplicand.n)
operator fun Fib.minus(divisor: Fib) = fib(n - divisor.n)
