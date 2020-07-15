package x.scratch

import x.scratch.BigRational.Companion.ONE
import x.scratch.BigRational.Companion.ZERO
import java.util.Objects.hash

sealed class BigRationalIterator(
    first: BigRational,
    protected val last: BigRational,
    private val step: BigRational,
) : Iterator<BigRational> {
    init {
        if (!step.isFinite()) error("Non-finite step.")
        if (!first.isFinite() || !last.isFinite())
            error("Non-finite bounds.")
        if (step == ZERO) error("Step must be non-zero.")
    }

    protected var current = first

    override fun next(): BigRational {
        val next = current
        current += step
        return next
    }
}

class IncrementingBigRationalIterator(
    /** The first element in the progression. */
    first: BigRational,
    /** The last element in the progression. */
    last: BigRational,
    step: BigRational,
) : BigRationalIterator(first, last, step) {
    init {
        if (first > last)
            error("Step must be advance range to avoid overflow.")
    }

    override fun hasNext() = current <= last
}

class DecrementingBigRationalIterator(
    /** The first element in the progression. */
    first: BigRational,
    /** The last element in the progression. */
    last: BigRational,
    step: BigRational,
) : BigRationalIterator(first, last, step) {
    init {
        if (first < last)
            error("Step must be advance range to avoid overflow.")
    }

    override fun hasNext() = current >= last
}

class BigRationalProgression(
    override val start: BigRational,
    override val endInclusive: BigRational,
    private val step: BigRational,
) : Iterable<BigRational>, ClosedRange<BigRational> {
    override fun iterator() =
        if (step < ZERO)
            DecrementingBigRationalIterator(start, endInclusive, step)
        else
            IncrementingBigRationalIterator(start, endInclusive, step)

    override fun equals(other: Any?) = this === other ||
            other is BigRationalProgression &&
            javaClass == other.javaClass &&
            start == other.start &&
            endInclusive == other.endInclusive &&
            step == other.step

    override fun hashCode() = hash(javaClass, start, endInclusive, step)

    override fun toString() =
        if (step < ZERO) "$start downTo $endInclusive step $step"
        else "$start..$endInclusive step $step"
}

/** Creates a range from this value to [endInclusive]. */
operator fun BigRational.rangeTo(endInclusive: BigRational) =
    BigRationalProgression(this, endInclusive, ONE)

/** Creates a range from this value to [endInclusive]. */
operator fun BigRational.rangeTo(endInclusive: BDouble) =
    this..(endInclusive.toBigRational())

/** Creates a range from this value to [endInclusive]. */
operator fun BDouble.rangeTo(endInclusive: BigRational) =
    toBigRational()..endInclusive

/** Creates a range from this value to [endInclusive]. */
operator fun BigRational.rangeTo(endInclusive: Double) =
    this..(endInclusive.toBigRational())

/** Creates a range from this value to [endInclusive]. */
operator fun Double.rangeTo(endInclusive: BigRational) =
    toBigRational()..endInclusive

/** Creates a range from this value to [endInclusive]. */
operator fun BigRational.rangeTo(endInclusive: Float) =
    this..(endInclusive.toBigRational())

/** Creates a range from this value to [endInclusive]. */
operator fun Float.rangeTo(endInclusive: BigRational) =
    toBigRational()..endInclusive

/** Creates a range from this value to [endInclusive]. */
operator fun BigRational.rangeTo(endInclusive: BInt) =
    this..(endInclusive.toBigRational())

/** Creates a range from this value to [endInclusive]. */
operator fun BInt.rangeTo(endInclusive: BigRational) =
    toBigRational()..endInclusive

/** Creates a range from this value to [endInclusive]. */
operator fun BigRational.rangeTo(endInclusive: Long) =
    this..(endInclusive.toBigRational())

/** Creates a range from this value to [endInclusive]. */
operator fun Long.rangeTo(endInclusive: BigRational) =
    toBigRational()..endInclusive

/** Creates a range from this value to [endInclusive]. */
operator fun BigRational.rangeTo(endInclusive: Int) =
    this..(endInclusive.toBigRational())

/** Creates a range from this value to [endInclusive]. */
operator fun Int.rangeTo(endInclusive: BigRational) =
    toBigRational()..endInclusive

/** Creates a range from this value _down_ to [endInclusive]. */
infix fun BigRational.downTo(endInclusive: BigRational) =
    BigRationalProgression(this, endInclusive, -ONE)

/** Creates a range from this value _down_ to [endInclusive]. */
infix fun BInt.downTo(endInclusive: BigRational) =
    toBigRational() downTo endInclusive

/** Creates a range from this value _down_ to [endInclusive]. */
infix fun Long.downTo(endInclusive: BigRational) =
    toBigRational() downTo endInclusive

/** Creates a range from this value _down_ to [endInclusive]. */
infix fun Int.downTo(endInclusive: BigRational) =
    toBigRational() downTo endInclusive

infix fun BigRationalProgression.step(step: BigRational) =
    BigRationalProgression(start, endInclusive, step)

infix fun BigRationalProgression.step(step: BInt) =
    BigRationalProgression(start, endInclusive, step.toBigRational())

infix fun BigRationalProgression.step(step: Long) =
    BigRationalProgression(start, endInclusive, step.toBigRational())

infix fun BigRationalProgression.step(step: Int) =
    BigRationalProgression(start, endInclusive, step.toBigRational())
