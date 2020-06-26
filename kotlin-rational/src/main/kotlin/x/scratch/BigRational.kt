package x.scratch

import x.scratch.BigRational.Companion.NEGATIVE_INFINITY
import x.scratch.BigRational.Companion.NaN
import x.scratch.BigRational.Companion.ONE
import x.scratch.BigRational.Companion.POSITIVE_INFINITY
import x.scratch.BigRational.Companion.ZERO
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Objects.hash
import kotlin.math.sign

internal typealias BRat = BigRational
internal typealias BInt = BigInteger
internal typealias BDouble = BigDecimal

class BigRational private constructor(
    val numerator: BInt,
    val denominator: BInt
) : Comparable<BRat>, Number() {
    val sign
        get() = when {
            isNaN() -> NaN
            else -> when (numerator.signum()) {
                -1 -> NEGATIVE_ONE
                0 -> ZERO
                else -> ONE
            }
        }

    val reciprocal: BRat get() = unaryDiv()

    fun signum() = sign // primitives and BInt differ here :(
    fun isFinite() = BInt.ZERO != denominator
    fun isNaN() = BInt.ZERO == numerator && BInt.ZERO == denominator
    fun isInteger() = BInt.ONE == denominator

    fun toBigDecimal(): BDouble = when (denominator) {
        BInt.ZERO -> throw ArithmeticException("Not finite.")
        else -> numerator.toBigDecimal().divide(denominator.toBigDecimal())
    }

    override fun toDouble() = when (denominator) {
        BInt.ZERO -> when (numerator) {
            BInt.ZERO -> Double.NaN
            BInt.ONE -> Double.POSITIVE_INFINITY
            else -> Double.NEGATIVE_INFINITY
        }
        else -> numerator.toBigDecimal().divide(denominator.toBigDecimal())
            .toDouble()
    }

    override fun toFloat() = when (denominator) {
        BInt.ZERO -> when (numerator) {
            BInt.ZERO -> Float.NaN
            BInt.ONE -> Float.POSITIVE_INFINITY
            else -> Float.NEGATIVE_INFINITY
        }
        else -> numerator.toBigDecimal().divide(denominator.toBigDecimal())
            .toFloat()
    }

    fun toBigInteger() =
        if (isInteger()) numerator
        else numerator / denominator

    override fun toLong() = toBigInteger().toLong()
    override fun toInt() = toBigInteger().toInt()
    override fun toShort() = throw UnsupportedOperationException()
    override fun toChar() = throw UnsupportedOperationException()
    override fun toByte() = throw UnsupportedOperationException()

    override fun compareTo(other: BRat) = when {
        this === other -> 0 // Sort stability for constants
        isNaN() || other.isNaN() -> 0 // Sorts like primitives for NaN
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
        val NEGATIVE_ONE = BRat(-BInt.ONE, BInt.ONE) // Much used
        val TWO = BRat(BInt.TWO, BInt.ONE)
        val NEGATIVE_TWO = BRat(-BInt.TWO, BInt.ONE)
        val TEN = BRat(BInt.TEN, BInt.ONE)
        val NEGATIVE_TEN = BRat(-BInt.TEN, BInt.ONE)
        val POSITIVE_INFINITY = BRat(BInt.ONE, BInt.ZERO)
        val NEGATIVE_INFINITY = BRat(-BInt.ONE, BInt.ZERO)
        val NaN = BRat(BInt.ZERO, BInt.ZERO)

        private val NegBIntONE = -BInt.ONE
        private val NegBIntTWO = -BInt.TWO
        private val NegBIntTEN = -BInt.TEN

        fun valueOf(numerator: BInt, denominator: BInt): BRat {
            // Not a function returning pair -- avoid extra function call
            // and destructuring
            var n = numerator
            var d = denominator
            if (-1 == d.signum()) {
                n = n.negate()
                d = d.negate()
            }

            if (BInt.ZERO == d) return when {
                1 == n.signum() -> POSITIVE_INFINITY
                -1 == n.signum() -> NEGATIVE_INFINITY
                else -> NaN
            }

            if (BInt.ZERO == n) return ZERO

            val gcd = n.gcd(d)
            n /= gcd
            d /= gcd

            // Note: BigInteger is sloppy about returning constants: test by
            // value rather than identity, even though identity would be
            // correct given the existence of the constants
            if (BInt.ONE == d) when (n) {
                BInt.ONE -> return ONE
                NegBIntONE -> return NEGATIVE_ONE
                BInt.TWO -> return TWO
                NegBIntTWO -> return NEGATIVE_TWO
                BInt.TEN -> return TEN
                NegBIntTEN -> return NEGATIVE_TEN
            }

            return BRat(n, d)
        }
    }
}

// TODO: How to handle the combinatorial explosion of overloads for `over`?

infix fun BInt.over(denominator: BInt) =
    BRat.valueOf(this, denominator)

infix fun Int.over(denominator: Int) =
    toBigInteger() over denominator.toBigInteger()

fun BDouble.toBigRational(): BRat {
    val scale = scale() // Key: read the javadoc for this call

    // This could be pulled out to a function returning a Pair, however I do
    // not want the garbage overhead of an additional ephemeral object.
    // TODO: Rethink performance vs clarity
    val numerator: BInt
    val denominator: BInt
    when (scale.sign) {
        0 -> {
            numerator = unscaledValue()
            denominator = BInt.ONE
        }
        -1 -> {
            numerator = unscaledValue() * BInt.TEN.pow(-scale)
            denominator = BInt.ONE
        }
        else -> {
            numerator = unscaledValue()
            denominator = BInt.TEN.pow(scale)
        }
    }

    return BRat.valueOf(numerator, denominator)
}

fun Double.toBigRational() = when {
    Double.POSITIVE_INFINITY == this -> POSITIVE_INFINITY
    Double.NEGATIVE_INFINITY == this -> NEGATIVE_INFINITY
    isNaN() -> NaN
    else -> toBigDecimal().toBigRational()
}

fun Float.toBigRational() = when {
    Float.POSITIVE_INFINITY == this -> POSITIVE_INFINITY
    Float.NEGATIVE_INFINITY == this -> NEGATIVE_INFINITY
    isNaN() -> NaN
    else -> toBigDecimal().toBigRational()
}

fun BInt.toBigRational() = BRat.valueOf(this, BInt.ONE)
fun Long.toBigRational() = toBigInteger().toBigRational()
fun Int.toBigRational() = toBigInteger().toBigRational()

operator fun BRat.unaryPlus() = this
operator fun BRat.unaryMinus() = BRat.valueOf(-numerator, denominator)

/** No such operator :) */
fun BRat.unaryDiv() = BRat.valueOf(denominator, numerator)

operator fun BRat.plus(addend: BRat) = BRat.valueOf(
    numerator * addend.denominator + addend.numerator * denominator,
    denominator * addend.denominator
)

operator fun BRat.minus(subtrahend: BRat) = this + -subtrahend
operator fun BRat.times(multiplier: BRat) = BRat.valueOf(
    numerator * multiplier.numerator,
    denominator * multiplier.denominator
)

operator fun BRat.div(divisor: BRat) = this * divisor.unaryDiv()
operator fun BRat.rem(@Suppress("UNUSED_PARAMETER") divisor: BRat) = when {
    isNaN() || divisor.isNaN() -> NaN
    else -> ZERO // All divisions are exact
}

operator fun BRat.inc() = this + ONE
operator fun BRat.dec() = this - ONE

fun BRat.pow(exponent: Int) = when {
    isNaN() -> NaN
    POSITIVE_INFINITY == this -> POSITIVE_INFINITY
    NEGATIVE_INFINITY == this ->
        if (exponent.isEven()) POSITIVE_INFINITY
        else NEGATIVE_INFINITY
    0 == exponent -> ONE
    0 > exponent -> BRat.valueOf(
        denominator.pow(-exponent), numerator.pow(-exponent)
    )
    else -> BRat.valueOf(numerator.pow(exponent), denominator.pow(exponent))
}

fun BRat.mediant(other: BRat) = when {
    isNaN() || other.isNaN() -> NaN
    (POSITIVE_INFINITY == this && NEGATIVE_INFINITY == other) ||
            (NEGATIVE_INFINITY == this && POSITIVE_INFINITY == other) -> ZERO
    else -> BRat.valueOf(
        numerator + other.numerator,
        denominator + other.denominator
    )
}

fun BRat.gcd(other: BRat) = BRat.valueOf(
    (numerator * other.denominator).gcd(other.numerator * denominator),
    denominator * other.denominator
)

fun BRat.floor() = when {
    !isFinite() || isInteger() -> this
    ONE == sign -> truncate()
    else -> truncate() - ONE
}

fun BRat.ceil() = when {
    !isFinite() || isInteger() -> this
    ONE == sign -> truncate() + ONE
    else -> truncate()
}

fun BRat.truncate() = when {
    !isFinite() || isInteger() -> this
    else -> BRat.valueOf(numerator / denominator, BInt.ONE)
}

fun BRat.divideAndRemainder(other: BigRational): Pair<BRat, BRat> {
    val quotient = (this / other).truncate()
    val remainder = this - other * quotient

    return quotient to remainder
}

private fun Int.isEven() = 0 == this % 2
