package x.scratch

import java.math.BigDecimal
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.TEN
import java.math.BigInteger.TWO
import java.math.BigInteger.ZERO

fun main() {
    println("== FLOATING POINT")

    println("FOO-AND-REMAINDER FUNCTIONS")
    println("√2 -> ${TWO.sqrtAndRemainder()!!.contentToString()}")

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
        -0.0,
        Double.MAX_VALUE,
        Double.MIN_VALUE,
        Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY,
        Double.NaN
    ))
        printRoundTrip(d)

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
        -0.0f,
        Float.MAX_VALUE,
        Float.MIN_VALUE,
        Float.POSITIVE_INFINITY,
        Float.NEGATIVE_INFINITY,
        Float.NaN
    ))
        printRoundTrip(d)
}

private fun printRoundTrip(floatingPoint: Double) {
    fun Double.display() = when (this) {
        Double.MAX_VALUE -> "MAX_VALUE"
        Double.MIN_VALUE -> "MIN_VALUE"
        else -> floatingPoint.toString()
    }

    val ratio = floatingPoint.toRatio()
    val backAgain = ratio.toDouble()

    println("${floatingPoint.display()} -> $ratio -> ${backAgain.display()}")

    if (floatingPoint == backAgain) return
    if (backAgain.isNaN() and floatingPoint.isNaN()) return

    error("DID NOT ROUND TRIP: $floatingPoint")
}

private fun printRoundTrip(floatingPoint: Float) {
    fun Float.display() = when (this) {
        Float.MAX_VALUE -> "MAX_VALUE"
        Float.MIN_VALUE -> "MIN_VALUE"
        else -> floatingPoint.toString()
    }

    val ratio = floatingPoint.toRatio()
    val backAgain = ratio.toFloat()

    println("${floatingPoint.display()} -> $ratio -> ${backAgain.display()}")

    if (floatingPoint == backAgain) return
    if (backAgain.isNaN() and floatingPoint.isNaN()) return

    error("DID NOT ROUND TRIP: $floatingPoint")
}

private fun Double.toRatio(): Pair<BigInteger, BigInteger> {
    return when {
        isNaN() -> ZERO to ZERO
        Double.POSITIVE_INFINITY == this -> ONE to ZERO
        Double.NEGATIVE_INFINITY == this -> -ONE to ZERO
        else -> toBigDecimal().toRatio()
    }
}

private fun Float.toRatio(): Pair<BigInteger, BigInteger> {
    return when {
        isNaN() -> ZERO to ZERO
        Float.POSITIVE_INFINITY == this -> ONE to ZERO
        Float.NEGATIVE_INFINITY == this -> -ONE to ZERO
        else -> toBigDecimal().toRatio()
    }
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

private fun Pair<BigInteger, BigInteger>.toDouble() = when (second) {
    ZERO -> when (first) {
        ZERO -> Double.NaN
        ONE -> Double.POSITIVE_INFINITY
        else -> Double.NEGATIVE_INFINITY
    }
    else -> first.toBigDecimal().divide(second.toBigDecimal()).toDouble()
}

private fun Pair<BigInteger, BigInteger>.toFloat() = when (second) {
    ZERO -> when (first) {
        ZERO -> Float.NaN
        ONE -> Float.POSITIVE_INFINITY
        else -> Float.NEGATIVE_INFINITY
    }
    else -> first.toBigDecimal().divide(second.toBigDecimal()).toFloat()
}
