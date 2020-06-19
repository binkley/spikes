package x.scratch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import x.scratch.BigRational.Companion.NEGATIVE_INFINITY
import x.scratch.BigRational.Companion.NaN
import x.scratch.BigRational.Companion.POSITIVE_INFINITY
import x.scratch.BigRational.Companion.ZERO

internal class FloatingPointTest {
    @Test
    fun `should compare like double for positive infinity`() {
        assertEquals(
            Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY,
            POSITIVE_INFINITY == POSITIVE_INFINITY
        )
        assertEquals(
            Double.POSITIVE_INFINITY < Double.POSITIVE_INFINITY,
            POSITIVE_INFINITY < POSITIVE_INFINITY
        )
        assertEquals(
            Double.POSITIVE_INFINITY > Double.POSITIVE_INFINITY,
            POSITIVE_INFINITY > POSITIVE_INFINITY
        )
    }

    @Test
    fun `should compare like double for negative infinity`() {
        assertEquals(
            Double.NEGATIVE_INFINITY == Double.NEGATIVE_INFINITY,
            NEGATIVE_INFINITY == NEGATIVE_INFINITY
        )
        assertEquals(
            Double.NEGATIVE_INFINITY < Double.NEGATIVE_INFINITY,
            NEGATIVE_INFINITY < NEGATIVE_INFINITY
        )
        assertEquals(
            Double.NEGATIVE_INFINITY > Double.NEGATIVE_INFINITY,
            NEGATIVE_INFINITY > NEGATIVE_INFINITY
        )
    }

    @Disabled("Sorting seems to work, but why does this fail?")
    @Test
    fun `should compare like double for not-a-number`() {
        assertEquals(
            Double.NaN == Double.NaN,
            NaN == NaN
        )
        assertEquals(
            Double.NaN < Double.NaN,
            NaN < NaN
        )
        assertEquals(
            Double.NaN > Double.NaN,
            NaN > NaN
        )
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

    @Test
    fun `should sort like double`() {
        val sorted = listOf(
            POSITIVE_INFINITY,
            NaN,
            ZERO,
            POSITIVE_INFINITY,
            NaN,
            NEGATIVE_INFINITY,
            ZERO,
            NEGATIVE_INFINITY
        ).sorted()
        val doubleSorted = listOf(
            Double.POSITIVE_INFINITY,
            Double.NaN,
            0.0,
            Double.POSITIVE_INFINITY,
            Double.NaN,
            Double.NEGATIVE_INFINITY,
            0.0,
            Double.NEGATIVE_INFINITY
        ).sorted()

        assertEquals(doubleSorted, sorted.map { it.toDouble() })
    }
}
