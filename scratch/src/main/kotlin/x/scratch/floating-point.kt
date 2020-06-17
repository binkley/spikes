package x.scratch

import java.math.BigDecimal
import java.math.BigInteger
import java.math.BigInteger.TEN
import kotlin.Double.Companion.NaN

fun main() {
    println("== FLOATING POINT")

    println()
    println("FP 0.1 -> ${0.1}")
    println("FP 0.1 + 0.2 -> ${0.1 + 0.2}")
    println("(0.1+0.2).toBig -> ${(0.1 + 0.2).toBigDecimal()}")
    println(
        "(0.1+0.2).toBig.toFP -> ${(0.1 + 0.2).toBigDecimal().toDouble()}"
    )

    println()
    println("NAN")
    @Suppress("ConvertNaNEquality")
    println("NaN == NaN -> ${NaN == NaN}")
    println("NaN.isNan -> ${NaN.isNaN()}")

    println()
    println("RATIOS")
    for (d in listOf(10.0, 1.0, 0.0, 0.1, 0.01, 0.1 + 0.2))
        println(ratioOf(d))
}

private fun ratioOf(d: Double): Pair<BigInteger, BigInteger> {
    val bigDecimal = BigDecimal.valueOf(d)
    val numerator = bigDecimal.unscaledValue()
    val denominator = TEN.pow(bigDecimal.scale())
    val gcd = numerator.gcd(denominator)

    return numerator / gcd to denominator / gcd
}
