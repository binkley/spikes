package x.scratch

import java.math.BigInteger
import java.util.Objects

/**
 * See https://developer.android.com/reference/kotlin/android/util/Rational
 *
 * @todo Consider LCM -- for simplicity avoid prime factorization for now
 * @todo Propagate NaN-ness
 */
class Rational private constructor(
    val numerator: BigInteger,
    val denominator: BigInteger
) : Number(), Comparable<Rational> {
    override fun toByte() = toLong().toByte()

    override fun toChar(): Char {
        // TODO: Think about converting to a Char
        error("Characters are inherently non-numeric")
    }

    override fun toDouble() =
        numerator.toDouble() / denominator.toDouble()

    override fun toFloat() =
        numerator.toFloat() / denominator.toFloat()

    override fun toInt() = toLong().toInt()

    override fun toLong() = (numerator / denominator).toLong()

    override fun toShort() = toLong().toShort()

    override fun compareTo(other: Rational): Int {
        // TODO: Find LCM rather than always using product
        // TODO: Handle corner cases with NaN and the infinities
        val a = numerator * other.denominator
        val b = other.numerator * denominator
        return a.compareTo(b)
    }

    override fun equals(other: Any?) = this === other
            || other is Rational
            && numerator == other.numerator
            && denominator == other.denominator

    override fun hashCode() = Objects.hash(numerator, denominator)

    override fun toString(): String {
        if (this == NaN) return "NaN"
        if (this == ZERO) return "0"
        if (this == POSITIVE_INFINITY) return "+∞"
        if (this == NEGATIVE_INFINITY) return "-∞"
        if (denominator == BigInteger.ONE) return numerator.toString()
        return "$numerator/$denominator"
    }

    operator fun unaryPlus() = this
    operator fun unaryMinus() = Rational(numerator.negate(), denominator)
    operator fun inc() = Rational(numerator + denominator, denominator)
    operator fun dec() = Rational(numerator - denominator, denominator)
    operator fun plus(b: Rational) = Rational(
        numerator * b.denominator + b.numerator * denominator,
        denominator * b.denominator
    )

    operator fun minus(b: Rational) = Rational(
        numerator * b.denominator - b.numerator * denominator,
        denominator * b.denominator
    )

    operator fun times(b: Rational) =
        Rational(numerator * b.numerator, denominator * b.denominator)

    operator fun div(b: Rational) =
        Rational(numerator * b.denominator, denominator * b.numerator)

    // TODO: operator fun rem
    // TODO: operator fun rangeTo

    // TODO: Comparisons to special values
    fun isFinite() = denominator != BigInteger.ZERO

    // TODO: Comparisons to special values
    fun isInfinite() =
        denominator == BigInteger.ZERO && numerator != BigInteger.ZERO

    // TODO: Comparisons to special values
    fun isNaN() =
        denominator == BigInteger.ZERO && numerator == BigInteger.ZERO

    // TODO: Comparisons to special values
    fun isZero() =
        denominator != BigInteger.ZERO && numerator == BigInteger.ZERO

    companion object {
        // TODO: Consider alternative of Rational as a sealed class, with
        //  special cases able to handle themselves, eg, toString
        val NaN = Rational(BigInteger.ZERO, BigInteger.ZERO)
        val ZERO = Rational(BigInteger.ZERO, BigInteger.ONE)
        val POSITIVE_INFINITY = Rational(BigInteger.ONE, BigInteger.ZERO)
        val NEGATIVE_INFINITY =
            Rational(BigInteger.ONE.negate(), BigInteger.ZERO)

        fun new(numerator: BigInteger, denominator: BigInteger): Rational {
            var n = numerator
            var d = denominator
            if (d < BigInteger.ZERO) {
                n = n.negate()
                d = d.negate()
            }

            tailrec fun gcd(p: BigInteger, q: BigInteger): BigInteger {
                if (q == BigInteger.ZERO) return p
                return gcd(q, p % q)
            }

            val gcd = gcd(n, d).abs()

            // TODO: Corner cases, like a 0 divisor, or returning object
            //  constants for special values
            return Rational(n / gcd, d / gcd)
        }
    }
}
