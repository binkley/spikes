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

    override fun equals(other: Any?) = when {
        isNaN() -> false
        this === other -> true
        other !is Rational -> false
        other.isNaN() -> false
        else -> numerator == other.numerator
                && denominator == other.denominator
    }

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
    operator fun unaryMinus() = new(numerator.negate(), denominator)
    operator fun inc() = new(numerator + denominator, denominator)
    operator fun dec() = new(numerator - denominator, denominator)
    operator fun plus(b: Rational) = new(
        numerator * b.denominator + b.numerator * denominator,
        denominator * b.denominator
    )

    operator fun minus(b: Rational) = new(
        numerator * b.denominator - b.numerator * denominator,
        denominator * b.denominator
    )

    operator fun times(b: Rational) =
        new(numerator * b.numerator, denominator * b.denominator)

    operator fun div(b: Rational) =
        new(numerator * b.denominator, denominator * b.numerator)

    // TODO: operator fun rem

    operator fun rangeTo(b: Rational) = RationalProgression(this, b)

    fun isFinite() =
        // TODO: Is NaN finite?
        this !== NaN && this !== POSITIVE_INFINITY && this !== NEGATIVE_INFINITY

    fun isInfinite() =
        this === POSITIVE_INFINITY || this === NEGATIVE_INFINITY

    fun isNaN() = this === NaN

    companion object {
        // TODO: Consider alternative of Rational as a sealed class, with
        //  special cases able to handle themselves, eg, toString
        val NaN = Rational(BigInteger.ZERO, BigInteger.ZERO)
        val ZERO = Rational(BigInteger.ZERO, BigInteger.ONE)
        val ONE = Rational(BigInteger.ONE, BigInteger.ONE)
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
            if (gcd != BigInteger.ZERO) {
                n /= gcd
                d /= gcd
            }

            fun BigInteger.isZero() = this == BigInteger.ZERO
            fun BigInteger.isOne() = this == BigInteger.ONE

            if (d.isZero()) when {
                n.isZero() -> return NaN
                n.isOne() -> return POSITIVE_INFINITY
                n.negate().isOne() -> return NEGATIVE_INFINITY
            }
            if (n.isZero()) return ZERO
            if (n.isOne() && d.isOne()) return ONE

            return Rational(n, d)
        }
    }
}

class RationalIterator(
    start: Rational,
    private val endInclusive: Rational,
    private val step: Rational
) : Iterator<Rational> {
    init {
        if (step == Rational.ZERO) error("Infinite loop")
    }

    private var current = start

    override fun hasNext() =
        if (step > Rational.ZERO)
            current <= endInclusive
        else
            current >= endInclusive

    override fun next(): Rational {
        val next = current
        current += step
        return next
    }
}

class RationalProgression(
    override val start: Rational,
    override val endInclusive: Rational,
    private val step: Rational = Rational.ONE
) : Iterable<Rational>, ClosedRange<Rational> {
    override fun iterator() = RationalIterator(start, endInclusive, step)

    infix fun step(step: Rational) =
        RationalProgression(start, endInclusive, step)
}

infix fun Rational.downTo(b: Rational) =
    RationalProgression(this, b, -Rational.ONE)
