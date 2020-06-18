package x.scratch

import java.math.BigDecimal
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.TEN

fun main() {
    println("== FLOATING POINT")

    println("RATIOS OF DOUBLES")

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
    printRoundTrip(Double.MAX_VALUE)
    println("MIN_VALUE")
    printRoundTrip(Double.MIN_VALUE)
    if (false) {
        println("POSITIVE_INFINITY")
        printRoundTrip(Double.POSITIVE_INFINITY)
        println("NEGATIVE_INFINITY")
        printRoundTrip(Double.NEGATIVE_INFINITY)
        println("NaN")
        printRoundTrip(Double.NaN)
    }

    println()
    println("RATIOS OF FLOATS")

    for (d in listOf(
        10.0f,
        1.0f,
        0.0f,
        0.1f,
        0.01f,
        0.1f + 0.2f,
        2.0f / 3.0f,
        -1.0f,
        -0.1f,
        -0.0f
    ))
        printRoundTrip(d)

    println("MAX_VALUE")
    printRoundTrip(Float.MAX_VALUE)
    println("MIN_VALUE")
    printRoundTrip(Float.MIN_VALUE)
    if (false) {
        println("POSITIVE_INFINITY")
        printRoundTrip(Float.POSITIVE_INFINITY)
        println("NEGATIVE_INFINITY")
        printRoundTrip(Float.NEGATIVE_INFINITY)
        println("NaN")
        printRoundTrip(Float.NaN)
    }
}

private fun printRoundTrip(floatingPoint: Double) {
    val ratio = floatingPoint.toBigDecimal().toRatio()
    val backAgain = ratio.toDouble()
    println("$floatingPoint -> $ratio -> $backAgain")
    if (floatingPoint != backAgain) error("DID NOT ROUNDTRIP: $floatingPoint")
}

private fun printRoundTrip(floatingPoint: Float) {
    val ratio = floatingPoint.toBigDecimal().toRatio()
    val backAgain = ratio.toFloat()
    println("$floatingPoint -> $ratio -> $backAgain")
    if (floatingPoint != backAgain) error("DID NOT ROUNDTRIP: $floatingPoint")
}

private fun BigDecimal.toRatio(): Pair<BigInteger, BigInteger> {
    val scale = scale()

    val numerator: BigInteger
    val denominator: BigInteger
    if (scale < 0) {
        numerator = unscaledValue() * TEN.pow(-scale)
        denominator = ONE
    } else {
        numerator = unscaledValue()
        denominator = TEN.pow(scale)
    }

    val gcd = numerator.gcd(denominator)

    return numerator / gcd to denominator / gcd
}

private fun Pair<BigInteger, BigInteger>.toDouble() =
    first.toBigDecimal().divide(second.toBigDecimal()).toDouble()

private fun Pair<BigInteger, BigInteger>.toFloat() =
    first.toBigDecimal().divide(second.toBigDecimal()).toFloat()
