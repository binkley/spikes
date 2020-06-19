package x.scratch

import x.scratch.BigRational.Companion.NEGATIVE_INFINITY
import x.scratch.BigRational.Companion.NaN
import x.scratch.BigRational.Companion.POSITIVE_INFINITY
import java.math.BigDecimal
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.TEN
import java.math.BigInteger.TWO
import java.math.BigInteger.ZERO
import java.util.Objects.hash

fun main() {
    println("== FLOATING POINT")

    fun header(text: String) {
        println()
        println(text)
    }

    header("PRELIMINARIES")

    for (d in listOf(
        Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY,
        Double.NaN
    ))
        for (e in listOf(
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NaN
        )) {
            println("EQ? $d $e -> ${d == e}")
            println("LT? $d $e -> ${d < e}")
            println("GT? $d $e -> ${d > e}")
        }

    header("FOO-AND-REMAINDER FUNCTIONS")

    println("√2 -> ${TWO.sqrtAndRemainder()!!.contentToString()}")

    header("RATIOS OF BIG RATIO NON-FINITE VALUES")

    for (r in listOf(POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN))
        for (s in listOf(POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN)) {
            println("EQ? ${r.display} ${s.display} -> ${r == s}")
            println("LT? ${r.display} ${s.display} -> ${r < s}")
            println("GT? ${r.display} ${s.display} -> ${r > s}")
        }

    header("COMPARE PRIMITIVES TO RATIOS")

    println("+∞ COMPARED")
    println(
        "EQ? ${(Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY)
                == (POSITIVE_INFINITY == POSITIVE_INFINITY)}"
    )
    println(
        "LT? ${(Double.POSITIVE_INFINITY < Double.POSITIVE_INFINITY)
                == (POSITIVE_INFINITY < POSITIVE_INFINITY)}"
    )
    println(
        "GT? ${(Double.POSITIVE_INFINITY > Double.POSITIVE_INFINITY)
                == (POSITIVE_INFINITY > POSITIVE_INFINITY)}"
    )
    println("-∞ COMPARED")
    println(
        "EQ? ${(Double.NEGATIVE_INFINITY == Double.NEGATIVE_INFINITY)
                == (NEGATIVE_INFINITY == NEGATIVE_INFINITY)}"
    )
    println(
        "LT? ${(Double.NEGATIVE_INFINITY < Double.NEGATIVE_INFINITY)
                == (NEGATIVE_INFINITY < NEGATIVE_INFINITY)}"
    )
    println(
        "GT? ${(Double.NEGATIVE_INFINITY > Double.NEGATIVE_INFINITY)
                == (NEGATIVE_INFINITY > NEGATIVE_INFINITY)}"
    )
    println("NaN COMPARED")
    println("EQ? ${(Double.NaN == Double.NaN) == (NaN == NaN)}")
    println("LT? ${(Double.NaN < Double.NaN) == (NaN < NaN)}")
    println("GT? ${(Double.NaN > Double.NaN) == (NaN > NaN)}")

    header("RATIOS OF DOUBLES")

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

    header("RATIOS OF FLOATS")

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

private val BigRational.display
    get() = when {
        POSITIVE_INFINITY == this -> "+∞"
        NEGATIVE_INFINITY == this -> "-∞"
        isNaN() -> "NaN"
        else -> toString()
    }

private fun printRoundTrip(floatingPoint: Double) {
    val ratio = floatingPoint.toRatio()
    val backAgain = ratio.toDouble()

    println("${floatingPoint.print} -> $ratio -> ${backAgain.print}")

    if (floatingPoint eq backAgain) return

    error("DID NOT ROUND TRIP: $floatingPoint")
}

private fun printRoundTrip(floatingPoint: Float) {
    val ratio = floatingPoint.toRatio()
    val backAgain = ratio.toFloat()

    println("${floatingPoint.print} -> $ratio -> ${backAgain.print}")

    if (floatingPoint eq backAgain) return

    error("DID NOT ROUND TRIP: $floatingPoint")
}

private typealias BInt = BigInteger
private typealias BDouble = BigDecimal
private typealias BRat = BigRational

internal class BigRational private constructor(
    val numerator: BigInteger,
    val denominator: BigInteger
) : Comparable<BRat> {
    fun isNaN() = BInt.ZERO == numerator && BInt.ZERO == denominator

    override fun compareTo(other: BRat) = when {
        this === other -> 0 // Sort stability for constants
        POSITIVE_INFINITY == this -> 1
        POSITIVE_INFINITY == other -> -1
        NEGATIVE_INFINITY == this -> -1
        NEGATIVE_INFINITY == other -> -1
        isNaN() || other.isNaN() -> 0 // NaN stays where it is
        else -> {
            val a = numerator * other.denominator
            val b = other.numerator * denominator
            a.compareTo(b)
        }
    }

    override fun equals(other: Any?) = !isNaN() && this === other ||
            other is BRat &&
            !other.isNaN() &&
            numerator == other.numerator &&
            denominator == other.denominator

    override fun hashCode() = hash(javaClass, numerator, denominator)

    override fun toString() = when {
        BInt.ZERO == denominator -> when {
            BInt.ONE == numerator -> "Infinity"
            -BInt.ONE == numerator -> "-Infinity"
            else -> "NaN"
        }
        BInt.ONE == denominator -> numerator.toString()
        else -> "$numerator/$denominator"
    }

    companion object {
        val ZERO = BRat(BInt.ZERO, BInt.ONE)
        val ONE = BRat(BInt.ONE, BInt.ONE)
        val TWO = BRat(BInt.TWO, BInt.ONE)
        val TEN = BRat(BInt.TEN, BInt.ONE)
        val POSITIVE_INFINITY = BRat(BInt.ONE, BInt.ZERO)
        val NEGATIVE_INFINITY = BRat(-BInt.ONE, BInt.ZERO)
        val NaN = BRat(BInt.ZERO, BInt.ZERO)

        fun valueOf(numerator: BigInteger, denominator: BigInteger): BRat {
            if (BInt.ZERO == denominator) return when {
                1 == numerator.signum() -> POSITIVE_INFINITY
                -1 == numerator.signum() -> NEGATIVE_INFINITY
                else -> NaN
            }

            if (BInt.ZERO == numerator) return ZERO

            var n = numerator
            var d = denominator
            if (-1 == d.signum()) {
                n = n.negate()
                d = d.negate()
            }

            val gcd = n.gcd(d)
            n /= gcd
            d /= gcd

            if (BInt.ONE == d) when (n) {
                BInt.ONE -> return ONE
                BInt.TWO -> return TWO
                BInt.TEN -> return TEN
            }

            return BRat(n, d)
        }
    }
}

private infix fun BInt.over(denominator: BInt) =
    BRat.valueOf(this, denominator)

private fun Double.toRatio(): BRat {
    return when {
        isNaN() -> ZERO over ZERO
        Double.POSITIVE_INFINITY == this -> ONE over ZERO
        Double.NEGATIVE_INFINITY == this -> -ONE over ZERO
        else -> toBigDecimal().toRatio()
    }
}

private fun Float.toRatio(): BRat {
    return when {
        isNaN() -> ZERO over ZERO
        Float.POSITIVE_INFINITY == this -> ONE over ZERO
        Float.NEGATIVE_INFINITY == this -> -ONE over ZERO
        else -> toBigDecimal().toRatio()
    }
}

private fun BigDecimal.toRatio(): BRat {
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

    return numerator / gcd over denominator / gcd
}

private fun BRat.toDouble() = when (denominator) {
    ZERO -> when (numerator) {
        ZERO -> Double.NaN
        ONE -> Double.POSITIVE_INFINITY
        else -> Double.NEGATIVE_INFINITY
    }
    else -> numerator.toBigDecimal().divide(denominator.toBigDecimal())
        .toDouble()
}

private fun BRat.toFloat() = when (denominator) {
    ZERO -> when (numerator) {
        ZERO -> Float.NaN
        ONE -> Float.POSITIVE_INFINITY
        else -> Float.NEGATIVE_INFINITY
    }
    else -> numerator.toBigDecimal().divide(denominator.toBigDecimal())
        .toFloat()
}

private val Double.print
    get() = when (this) {
        Double.MAX_VALUE -> "MAX_VALUE"
        Double.MIN_VALUE -> "MIN_VALUE"
        else -> toString()
    }

private infix fun Double.eq(other: Double) =
    this == other || this.isNaN() && other.isNaN()

private val Float.print
    get() = when (this) {
        Float.MAX_VALUE -> "MAX_VALUE"
        Float.MIN_VALUE -> "MIN_VALUE"
        else -> toString()
    }

private infix fun Float.eq(other: Float) =
    this == other || this.isNaN() && other.isNaN()
