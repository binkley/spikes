package x.scratch

fun main() {
    println("XX == $Fib0")
    println("XX 1/F0 -> ${Fib0.inv}")
    println("XX F0^2 -> ${Fib0.pow(2)}")
    println("XX F0^-2 -> ${Fib0.pow(-2)}")
    println("== $Fib1")
    println("1/F1 -> ${Fib1.inv}")
    println("F1^2 -> ${Fib1.pow(2)}")
    println("F1^-2 -> ${Fib1.pow(-2)}")
    println("== ${Fib1.inv}")
    println("1/(1/F1) -> ${Fib1.inv.inv}")
    println("(1/F1)^2 -> ${Fib1.inv.pow(2)}")
    println("(1/F1)^-2 -> ${Fib1.inv.pow(-2)}")
}

val Fib0 = Fib(0, 1, 0, 0, 1)
val Fib1 = Fib(1, 0, 1, 1, 1)

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
val Fib.inv
    get() : Fib {
        val det = det
        return Fib(-n, det * d, det * -b, det * -c, det * a)
    }

fun Fib.pow(p: Int): Fib {
    when (p) {
        0 -> return Fib0
        1 -> return this
        -1 -> return inv
    }

    val f = if (p < 0) inv else this
    var pp = if (p < 0) -p else p

    var pow = f
    while (pp > 0) {
        --pp
        pow *= f
    }
    return pow
}

operator fun Fib.times(multiplicand: Fib) =
    Fib(
        n + multiplicand.n,
        a * multiplicand.a + b * multiplicand.c,
        a * multiplicand.b + b * multiplicand.d,
        c * multiplicand.a + d * multiplicand.c,
        c * multiplicand.b + d * multiplicand.d
    )
