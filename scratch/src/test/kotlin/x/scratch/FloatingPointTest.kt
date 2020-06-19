package x.scratch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private typealias BRat = BigRational

internal class FloatingPointTest {
    @Test
    fun `should compare like double for positive infinity`() {
        assertEquals(
            Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY,
            BRat.POSITIVE_INFINITY == BRat.POSITIVE_INFINITY
        )
        assertEquals(
            Double.POSITIVE_INFINITY < Double.POSITIVE_INFINITY,
            BRat.POSITIVE_INFINITY < BRat.POSITIVE_INFINITY
        )
        assertEquals(
            Double.POSITIVE_INFINITY > Double.POSITIVE_INFINITY,
            BRat.POSITIVE_INFINITY > BRat.POSITIVE_INFINITY
        )
    }

    @Test
    fun `should compare like double for negative infinity`() {
        assertEquals(
            Double.NEGATIVE_INFINITY == Double.NEGATIVE_INFINITY,
            BRat.NEGATIVE_INFINITY == BRat.NEGATIVE_INFINITY
        )
        assertEquals(
            Double.NEGATIVE_INFINITY < Double.NEGATIVE_INFINITY,
            BRat.NEGATIVE_INFINITY < BRat.NEGATIVE_INFINITY
        )
        assertEquals(
            Double.NEGATIVE_INFINITY > Double.NEGATIVE_INFINITY,
            BRat.NEGATIVE_INFINITY > BRat.NEGATIVE_INFINITY
        )
    }

    @Test
    fun `should compare like double for not-a-number`() {
        assertEquals(
            Double.NaN == Double.NaN,
            BRat.NaN == BRat.NaN
        )
        assertEquals(
            Double.NaN < Double.NaN,
            BRat.NaN < BRat.NaN
        )
        assertEquals(
            Double.NaN > Double.NaN,
            BRat.NaN > BRat.NaN
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
}
