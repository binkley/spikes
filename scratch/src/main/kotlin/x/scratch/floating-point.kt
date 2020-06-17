package x.scratch

import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.TEN
import kotlin.Double.Companion.MAX_VALUE
import kotlin.Double.Companion.MIN_VALUE
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

    for (d in listOf(
        10.0,
        1.0,
        0.0,
        0.1,
        0.01,
        0.1 + 0.2,
        2.0 / 3.0,
        -1.0,
        -0.1,
        -0.0
    ))
        printRoundTrip(d)

    println("MAX_VALUE")
    printRoundTrip(MAX_VALUE)

    println("MIN_VALUE")
    printRoundTrip(MIN_VALUE)
}

private fun printRoundTrip(d: Double) {
    val ratio = d.toRatio()
    val backAgain = ratio.toDouble()
    println("$d -> $ratio -> $backAgain")
    if (d != backAgain) error("DID NOT ROUNDTRIP: $d")
}

private fun Double.toRatio(): Pair<BigInteger, BigInteger> {
    val big = toBigDecimal()
    val scale = big.scale()

    val numerator: BigInteger
    val denominator: BigInteger
    if (scale < 0) {
        numerator = big.unscaledValue() * TEN.pow(-scale)
        denominator = ONE
    } else {
        numerator = big.unscaledValue()
        denominator = TEN.pow(scale)
    }

    val gcd = numerator.gcd(denominator)

    return numerator / gcd to denominator / gcd
}

private fun Pair<BigInteger, BigInteger>.toDouble() =
    first.toBigDecimal().divide(second.toBigDecimal()).toDouble()
