package x.scratch

import java.math.BigInteger
import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN

fun main() {
    println("==MATH")

    println(" * BigDecimal")
    listOf(
        BigDecimal("1.5"),
        BigDecimal("0.5"),
        BigDecimal("-0.5"),
        BigDecimal("-1.5"),
    ).forEach {
        println("$it -> ${it.setScale(0, HALF_EVEN)}")
    }

    fun round(n: Int, d: Int) =
        BigDecimal.valueOf(n.toLong())
            .div(BigDecimal.valueOf(d.toLong()))
            .setScale(0, HALF_EVEN)

    println(" * BigInteger")
    listOf(
        3 to 2,
        1 to 2,
        -1 to 2,
        -3 to 2,
    ).forEach { (n, d) ->
        println("$n/$d -> ${round(n, d)}")
    }
}
