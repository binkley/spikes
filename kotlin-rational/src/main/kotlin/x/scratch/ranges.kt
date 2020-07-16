package x.scratch

import x.scratch.BigRational.Companion.ONE
import x.scratch.BigRational.Companion.ZERO
import java.util.Objects.hash

interface BigRationalRange : Iterable<BRat>, ClosedRange<BRat>

private typealias BRatRange = BigRationalRange

private sealed class BigRationalIterator(
    protected var current: BRat,
    protected val last: BRat,
    private val step: BRat,
) : Iterator<BRat> {
    init {
        if (!step.isFinite()) error("Non-finite step.")
        if (!current.isFinite() || !last.isFinite())
            error("Non-finite bounds.")
        if (ZERO == step) error("Step must be non-zero.")
    }

    override fun next(): BRat {
        val next = current
        current += step
        return next
    }
}

private class IncrementingBigRationalIterator(
    first: BRat,
    last: BRat,
    step: BRat,
) : BigRationalIterator(first, last, step) {
    init {
        if (first > last)
            error("Step must be advance range to avoid overflow.")
    }

    override fun hasNext() = current <= last
}

private class DecrementingBigRationalIterator(
    first: BRat,
    last: BRat,
    step: BRat,
) : BigRationalIterator(first, last, step) {
    init {
        if (first < last)
            error("Step must be advance range to avoid overflow.")
    }

    override fun hasNext() = current >= last
}

private class BigRationalProgression(
    override val start: BRat,
    override val endInclusive: BRat,
    private val step: BRat,
) : BRatRange {
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
        if (ZERO > step) "$start downTo $endInclusive step $step"
        else "$start..$endInclusive step $step"
}

/** Creates a range from this value to [endInclusive]. */
operator fun BRat.rangeTo(endInclusive: BRat): BRatRange =
    BigRationalProgression(this, endInclusive, ONE)

/** Creates a range from this value to [endInclusive]. */
operator fun BRat.rangeTo(endInclusive: BDouble) =
    this..(endInclusive.toBigRational())

/** Creates a range from this value to [endInclusive]. */
operator fun BDouble.rangeTo(endInclusive: BRat) =
    toBigRational()..endInclusive

/** Creates a range from this value to [endInclusive]. */
operator fun BRat.rangeTo(endInclusive: Double) =
    this..(endInclusive.toBigRational())

/** Creates a range from this value to [endInclusive]. */
operator fun Double.rangeTo(endInclusive: BRat) =
    toBigRational()..endInclusive

/** Creates a range from this value to [endInclusive]. */
operator fun BRat.rangeTo(endInclusive: Float) =
    this..(endInclusive.toBigRational())

/** Creates a range from this value to [endInclusive]. */
operator fun Float.rangeTo(endInclusive: BRat) =
    toBigRational()..endInclusive

/** Creates a range from this value to [endInclusive]. */
operator fun BRat.rangeTo(endInclusive: BInt) =
    this..(endInclusive.toBigRational())

/** Creates a range from this value to [endInclusive]. */
operator fun BInt.rangeTo(endInclusive: BRat) =
    toBigRational()..endInclusive

/** Creates a range from this value to [endInclusive]. */
operator fun BRat.rangeTo(endInclusive: Long) =
    this..(endInclusive.toBigRational())

/** Creates a range from this value to [endInclusive]. */
operator fun Long.rangeTo(endInclusive: BRat) =
    toBigRational()..endInclusive

/** Creates a range from this value to [endInclusive]. */
operator fun BRat.rangeTo(endInclusive: Int) =
    this..(endInclusive.toBigRational())

/** Creates a range from this value to [endInclusive]. */
operator fun Int.rangeTo(endInclusive: BRat) =
    toBigRational()..endInclusive

/** Creates a range from this value _down_ to [endInclusive]. */
infix fun BRat.downTo(endInclusive: BRat): BRatRange =
    BigRationalProgression(this, endInclusive, -ONE)

/** Creates a range from this value _down_ to [endInclusive]. */
infix fun BInt.downTo(endInclusive: BRat): BRatRange =
    toBigRational() downTo endInclusive

/** Creates a range from this value _down_ to [endInclusive]. */
infix fun Long.downTo(endInclusive: BRat): BRatRange =
    toBigRational() downTo endInclusive

/** Creates a range from this value _down_ to [endInclusive]. */
infix fun Int.downTo(endInclusive: BRat): BRatRange =
    toBigRational() downTo endInclusive

infix fun BRatRange.step(step: BRat): BRatRange =
    BigRationalProgression(start, endInclusive, step)

infix fun BRatRange.step(step: BInt): BRatRange =
    BigRationalProgression(start, endInclusive, step.toBigRational())

infix fun BRatRange.step(step: Long): BRatRange =
    BigRationalProgression(start, endInclusive, step.toBigRational())

infix fun BRatRange.step(step: Int): BRatRange =
    BigRationalProgression(start, endInclusive, step.toBigRational())
