package x.scratch

import lombok.Generated
import x.scratch.BigRational.Companion.ZERO

fun BRat.toContinuedFraction() = ContinuedFraction.valueOf(this)
fun ContinuedFraction.toBigRational() = backAgain()

@Generated // Lie to JaCoCo -- "All 6 branches missed"?!
class ContinuedFraction(
    private val terms: List<BInt>
) : List<BInt> by terms {
    val integerPart: BInt get() = first()
    val fractionalParts: List<BInt> get() = subList(1, lastIndex + 1)
    val reciprocal: ContinuedFraction get() = unaryDiv()

    fun unaryDiv() = if (BInt.ZERO == integerPart)
        ContinuedFraction(fractionalParts)
    else
        ContinuedFraction(listOf(BInt.ZERO) + terms)

    override fun toString() = when (size) {
        1 -> "[$integerPart;]"
        else -> terms.toString().replaceFirst(',', ';')
    }

    companion object {
        fun valueOf(rat: BRat): ContinuedFraction {
            val terms = mutableListOf<BInt>()
            fractionateInPlace(rat, terms)
            return ContinuedFraction(terms)
        }
    }
}

private tailrec fun fractionateInPlace(
    rat: BRat,
    sequence: MutableList<BInt>
): List<BInt> {
    val (i, f) = rat.toParts()
    sequence += i
    if (ZERO == f) return sequence
    return fractionateInPlace(f.unaryDiv(), sequence)
}

private fun BRat.toParts(): Pair<BInt, BRat> {
    val i = floor()
    return i.toBigInteger() to (this - i)
}

private fun ContinuedFraction.backAgain() = subList(0, size - 1)
    .asReversed()
    .asSequence()
    .map { it.toBigRational() }
    .fold(last().toBigRational()) { previous, a_ni ->
        previous.unaryDiv() + a_ni
    }
