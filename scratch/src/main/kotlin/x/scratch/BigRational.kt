package x.scratch

import x.scratch.BigRational.Companion.NEGATIVE_INFINITY
import x.scratch.BigRational.Companion.NaN
import x.scratch.BigRational.Companion.POSITIVE_INFINITY
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Objects

internal typealias BRat = BigRational
internal typealias BInt = BigInteger
internal typealias BDouble = BigDecimal

internal class BigRational private constructor(
    val numerator: BInt,
    val denominator: BInt
) : Comparable<BRat> {
    val reciprocal: BRat get() = unaryDiv()

    fun isNaN() =
        BInt.ZERO == numerator && BInt.ZERO == denominator

    fun toBigDecimal() = when (denominator) {
        BInt.ZERO -> throw ArithmeticException("Not finite.")
        else -> numerator.toBigDecimal().divide(denominator.toBigDecimal())
    }

    fun toDouble() = when (denominator) {
        BInt.ZERO -> when (numerator) {
            BInt.ZERO -> Double.NaN
            BInt.ONE -> Double.POSITIVE_INFINITY
            else -> Double.NEGATIVE_INFINITY
        }
        else -> numerator.toBigDecimal().divide(denominator.toBigDecimal())
            .toDouble()
    }

    fun toFloat() = when (denominator) {
        BInt.ZERO -> when (numerator) {
            BInt.ZERO -> Float.NaN
            BInt.ONE -> Float.POSITIVE_INFINITY
            else -> Float.NEGATIVE_INFINITY
        }
        else -> numerator.toBigDecimal().divide(denominator.toBigDecimal())
            .toFloat()
    }

    override fun compareTo(other: BRat) = when {
        isNaN() || other.isNaN() -> 0 // Sorts like primitives for NaN
        this === other -> 0 // Sort stability for constants
        POSITIVE_INFINITY == this -> 1
        POSITIVE_INFINITY == other -> -1
        NEGATIVE_INFINITY == this -> -1
        NEGATIVE_INFINITY == other -> 1
        else -> {
            val a = numerator * other.denominator
            val b = other.numerator * denominator
            a.compareTo(b)
        }
    }

    operator fun unaryPlus() = this
    operator fun unaryMinus() =
        BRat(-numerator, denominator) // Careful, ok to skip valueOf here

    fun unaryDiv() =
        BRat.valueOf(denominator, numerator) // No such operator :)

    operator fun plus(addend: BRat) = BRat.valueOf(
        numerator * addend.denominator + addend.numerator * denominator,
        denominator * addend.denominator
    )

    operator fun minus(subtrahend: BRat) = this + -subtrahend
    operator fun times(multiplicand: BRat) = BRat.valueOf(
        numerator * multiplicand.numerator,
        denominator * multiplicand.denominator
    )

    operator fun div(dividend: BRat) = this * dividend.unaryDiv()
    operator fun rem(dividend: BRat) = ZERO // All divisions are exact

    override fun equals(other: Any?) = !isNaN() && this === other ||
            other is BRat &&
            !other.isNaN() &&
            numerator == other.numerator &&
            denominator == other.denominator

    override fun hashCode() = Objects.hash(
        javaClass, numerator, denominator
    )

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

        fun valueOf(numerator: BInt, denominator: BInt): BRat {
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

// TODO: How to handle the combinatorial explosion of overloads for `over`?

internal infix fun BInt.over(denominator: BInt) =
    BRat.valueOf(this, denominator)

internal infix fun Long.over(denominator: Long) =
    toBigInteger() over denominator.toBigInteger()

internal infix fun Int.over(denominator: Int) =
    toBigInteger() over denominator.toBigInteger()

internal fun BDouble.toBigRational(): BRat {
    val scale = scale() // Key: read the javadoc for this call

    val numerator: BInt
    val denominator: BInt
    if (0 > scale) {
        numerator = unscaledValue() * BInt.TEN.pow(-scale)
        denominator = BInt.ONE
    } else {
        numerator = unscaledValue()
        denominator = BInt.TEN.pow(scale)
    }

    val gcd = numerator.gcd(denominator)

    return BRat.valueOf(numerator / gcd, denominator / gcd)
}

internal fun Double.toBigRational() = when {
    Double.POSITIVE_INFINITY == this -> POSITIVE_INFINITY
    Double.NEGATIVE_INFINITY == this -> NEGATIVE_INFINITY
    isNaN() -> NaN
    else -> toBigDecimal().toBigRational()
}

internal fun Float.toBigRational() = when {
    Float.POSITIVE_INFINITY == this -> POSITIVE_INFINITY
    Float.NEGATIVE_INFINITY == this -> NEGATIVE_INFINITY
    isNaN() -> NaN
    else -> toBigDecimal().toBigRational()
}

internal fun BInt.toBigRational() = BRat.valueOf(this, BInt.ONE)
internal fun Long.toBigRational() = toBigInteger().toBigRational()
internal fun Int.toBigRational() = toBigInteger().toBigRational()
