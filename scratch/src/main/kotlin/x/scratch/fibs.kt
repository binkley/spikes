package x.scratch

fun main() {
    println("SQUARE FIB0 -- IDENTITY")
    println(Fib0)
    println(Fib0.pow(2))
    println("SQUARE FIB1 -- BASE FIBONACCI")
    println(Fib1)
    println(Fib1.pow(2))
    println("SQUARE FIB-1 -- FIRST NEGATIVE FIB")
    println(Fib1.inv)
    println(Fib1.inv.pow(2))
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
    override fun toString() = "F$n:[$a, $b; $c, $d]"
}

val Fib.fib get() = b
val Fib.det get() = if (0 == n % 2) -1 else 1
val Fib.inv get() = Fib(-n, -d, b, c, -a)

fun Fib.pow(p: Int): Fib {
    val n = p + n
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
