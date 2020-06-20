package x.scratch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import x.scratch.BigRational.Companion.NEGATIVE_INFINITY
import x.scratch.BigRational.Companion.NaN
import x.scratch.BigRational.Companion.POSITIVE_INFINITY
import x.scratch.BigRational.Companion.ZERO

private val Pair<Double, BRat>.primitive get() = first
private val Pair<Double, BRat>.rational get() = second

internal class FloatingPointTest {
    @Test
    fun `should compare like double`() {
        listOf(
            0.0 to ZERO,
            Double.POSITIVE_INFINITY to POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY to NEGATIVE_INFINITY,
            Double.NaN to NaN
        ).cartesian().forEach { (a, b) ->
            assertEquals(
                a.primitive == b.primitive,
                a.rational == b.rational,
                "EQ? -> ${a.rational} == ${b.rational}"
            )
            assertEquals(
                a.primitive > b.primitive,
                a.rational > b.rational,
                "GT? -> ${a.rational} > ${b.rational}"
            )
            assertEquals(
                a.primitive < b.primitive,
                a.rational < b.rational,
                "LT? -> ${a.rational} < ${b.rational}"
            )
        }
    }

    @Test
    fun `should round trip to and from double`() {
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
            // -0.0, -- TODO: What to do about "negative 0"?
            Double.MAX_VALUE,
            -Double.MAX_VALUE,
            Double.MIN_VALUE,
            -Double.MIN_VALUE,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY
        )) assertEquals(d, d.toBigRational().toDouble())

        assertTrue(
            Double.NaN.toBigRational().toDouble().isNaN()
        )
    }
}
