package x.scratch

import java.math.BigDecimal
import java.math.BigInteger
import java.util.Objects

internal typealias BRat = BigRational
internal typealias BInt = BigInteger
internal typealias BDouble = BigDecimal

internal class BigRational private constructor(
    val numerator: BigInteger,
    val denominator: BigInteger
) : Comparable<BRat> {
    fun isNaN() =
        BigInteger.ZERO == numerator && BigInteger.ZERO == denominator

    fun toBigDecimal() = when (denominator) {
        BigInteger.ZERO -> throw ArithmeticException("Not finite.")
        else -> numerator.toBigDecimal().divide(denominator.toBigDecimal())
    }

    fun toDouble() = when (denominator) {
        BigInteger.ZERO -> when (numerator) {
            BigInteger.ZERO -> Double.NaN
            BigInteger.ONE -> Double.POSITIVE_INFINITY
            else -> Double.NEGATIVE_INFINITY
        }
        else -> numerator.toBigDecimal().divide(denominator.toBigDecimal())
            .toDouble()
    }

    fun toFloat() = when (denominator) {
        BigInteger.ZERO -> when (numerator) {
            BigInteger.ZERO -> Float.NaN
            BigInteger.ONE -> Float.POSITIVE_INFINITY
            else -> Float.NEGATIVE_INFINITY
        }
        else -> numerator.toBigDecimal().divide(denominator.toBigDecimal())
            .toFloat()
    }

    override fun compareTo(other: BRat) = when {
        isNaN() -> 1 // NaN sorts to end
        other.isNaN() -> -1
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

    override fun equals(other: Any?) = !isNaN() && this === other ||
            other is BRat &&
            !other.isNaN() &&
            numerator == other.numerator &&
            denominator == other.denominator

    override fun hashCode() = Objects.hash(
        javaClass, numerator, denominator
    )

    override fun toString() = when {
        BigInteger.ZERO == denominator -> when {
            BigInteger.ONE == numerator -> "Infinity"
            -BigInteger.ONE == numerator -> "-Infinity"
            else -> "NaN"
        }
        BigInteger.ONE == denominator -> numerator.toString()
        else -> "$numerator/$denominator"
    }

    companion object {
        val ZERO = BRat(
            BigInteger.ZERO,
            BigInteger.ONE
        )
        val ONE = BRat(
            BigInteger.ONE,
            BigInteger.ONE
        )
        val TWO = BRat(
            BigInteger.TWO,
            BigInteger.ONE
        )
        val TEN = BRat(
            BigInteger.TEN,
            BigInteger.ONE
        )
        val POSITIVE_INFINITY = BRat(
            BigInteger.ONE,
            BigInteger.ZERO
        )
        val NEGATIVE_INFINITY = BRat(
            -BigInteger.ONE,
            BigInteger.ZERO
        )
        val NaN = BRat(
            BigInteger.ZERO,
            BigInteger.ZERO
        )

        fun valueOf(numerator: BInt, denominator: BInt): BRat {
            if (BigInteger.ZERO == denominator) return when {
                1 == numerator.signum() -> POSITIVE_INFINITY
                -1 == numerator.signum() -> NEGATIVE_INFINITY
                else -> NaN
            }

            if (BigInteger.ZERO == numerator) return ZERO

            var n = numerator
            var d = denominator
            if (-1 == d.signum()) {
                n = n.negate()
                d = d.negate()
            }

            val gcd = n.gcd(d)
            n /= gcd
            d /= gcd

            if (BigInteger.ONE == d) when (n) {
                BigInteger.ONE -> return ONE
                BigInteger.TWO -> return TWO
                BigInteger.TEN -> return TEN
            }

            return BRat(n, d)
        }
    }
}

internal infix fun BInt.over(denominator: BInt) =
    BRat.valueOf(this, denominator)

internal fun Double.toBigRational(): BRat {
    return when {
        Double.POSITIVE_INFINITY == this -> BigRational.POSITIVE_INFINITY
        Double.NEGATIVE_INFINITY == this -> BigRational.NEGATIVE_INFINITY
        isNaN() -> BigRational.NaN
        else -> toBigDecimal().toBigRational()
    }
}

internal fun Float.toBigRational(): BRat {
    return when {
        Float.POSITIVE_INFINITY == this -> BigRational.POSITIVE_INFINITY
        Float.NEGATIVE_INFINITY == this -> BigRational.NEGATIVE_INFINITY
        isNaN() -> BigRational.NaN
        else -> toBigDecimal().toBigRational()
    }
}

internal fun BigDecimal.toBigRational(): BRat {
    val scale = scale()

    val numerator: BInt
    val denominator: BInt
    if (scale < 0) {
        numerator = unscaledValue() * BigInteger.TEN.pow(-scale)
        denominator = BigInteger.ONE
    } else {
        numerator = unscaledValue()
        denominator = BigInteger.TEN.pow(scale)
    }

    val gcd = numerator.gcd(denominator)

    return numerator / gcd over denominator / gcd
}
