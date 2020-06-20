package x.scratch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import x.scratch.BigRational.Companion.NEGATIVE_INFINITY
import x.scratch.BigRational.Companion.NaN
import x.scratch.BigRational.Companion.ONE
import x.scratch.BigRational.Companion.POSITIVE_INFINITY
import x.scratch.BigRational.Companion.ZERO

internal class BigRationalTest {
    @Nested
    inner class Conversions {
        @Test
        fun `should round trip from and to big decimal`() {
            for (n in listOf(
                BDouble.TEN,
                BDouble.ONE,
                BDouble.ZERO,
                BDouble.valueOf(1, 1), // 0.1
                BDouble.valueOf(1, 2), // 0.01
                BDouble.valueOf(1, 1) + BDouble.valueOf(2, 1), // 0.1+0.2
                BDouble.valueOf(2,) / BDouble.valueOf(3), // 2.0/3.0
                -BDouble.ONE,
                -BDouble.valueOf(1, 1) // -0.1
            )) assertEquals(
                n, n.toBigRational().toBigDecimal(),
                "EQ? -> big decimal <-> rational"
            )
        }

        @Test
        fun `should round trip from and to double`() {
            for (n in listOf(
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
                Double.NEGATIVE_INFINITY,
                Double.NaN
            )) assertTrue(
                n.eq(n.toBigRational().toDouble()),
                "EQ? double <-> rational -> $n"
            )
        }

        @Test
        fun `should round trip from and to float`() {
            for (n in listOf(
                10.0f,
                1.0f,
                0.0f,
                0.1f,
                0.01f,
                0.1f + 0.2f,
                2.0f / 3.0f,
                -1.0f,
                -0.1f,
                // -0.0, -- TODO: What to do about "negative 0"?
                Float.MAX_VALUE,
                -Float.MAX_VALUE,
                Float.MIN_VALUE,
                -Float.MIN_VALUE,
                Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                Float.NaN
            )) assertTrue(
                n.eq(n.toBigRational().toFloat()),
                "EQ? float <-> rational -> $n"
            )
        }

        @Test
        fun `should round trip from and to big integer`() {
            for (n in listOf(
                BInt.TEN,
                BInt.ONE,
                BInt.ZERO,
                -BInt.ONE
            )) assertEquals(
                n,
                n.toBigRational().toBigInteger(),
                "EQ? big integer <-> rational -> $n"
            )
        }

        @Test
        fun `should round trip from and to long`() {
            for (n in listOf(
                10L,
                1L,
                0L,
                -1L
            )) assertEquals(
                n,
                n.toBigRational().toLong(),
                "EQ? long <-> rational -> $n"
            )
        }

        @Test
        fun `should round trip from and to int`() {
            for (n in listOf(
                10,
                1,
                0,
                -1
            )) assertEquals(
                n,
                n.toBigRational().toInt(),
                "EQ? int <-> rational -> $n"
            )
        }

        @Test
        fun `should not convert to short`() {
            assertThrows<UnsupportedOperationException> {
                ONE.toShort()
            }
        }

        @Test
        fun `should not convert to char`() {
            assertThrows<UnsupportedOperationException> {
                ONE.toChar()
            }
        }

        @Test
        fun `should not convert to byte`() {
            assertThrows<UnsupportedOperationException> {
                ONE.toByte()
            }
        }
    }

    @Nested
    inner class FidelityToPrimities {
        @Test
        fun `should compare as primitives do`() {
            listOf(
                0.0 to ZERO,
                +1.0 to +ONE,
                -1.0 to -ONE,
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
        fun `should treat negation of non-finite values as primitive do`() {
            assertEquals(NEGATIVE_INFINITY, -POSITIVE_INFINITY)
            assertEquals(POSITIVE_INFINITY, -NEGATIVE_INFINITY)
            assertNotEquals(-NaN, -NaN)
        }

        @Test
        fun `should print as primitives do`() {
            assertEquals("${Double.POSITIVE_INFINITY}", "$POSITIVE_INFINITY")
            assertEquals("${Double.NEGATIVE_INFINITY}", "$NEGATIVE_INFINITY")
            assertEquals("${Double.NaN}", "$NaN")
        }
    }

    @Nested
    inner class Stringifying {
        @Test
        fun `should print nicely`() {
            assertEquals("0", "$ZERO")
            assertEquals("1", "$ONE")
            assertEquals("-1", "${-ONE}")
            assertEquals("1/10", "${1 over 10}")
            assertEquals("-1/10", "${-1 over 10}")
        }
    }

    @Nested
    inner class Arithmetic {
        @Test
        fun `should add`() {
            assertEquals(19 over 15, (2 over 3) + (3 over 5))
        }

        @Test
        fun `should subtract`() {
            assertEquals(1 over 15, (2 over 3) - (3 over 5))
        }

        @Test
        fun `should multiply`() {
            assertEquals(6 over 15, (2 over 3) * (3 over 5))
        }

        @Test
        fun `should divide`() {
            assertEquals(10 over 9, (2 over 3) / (3 over 5))
        }

        @Test
        fun `should have no remainder as division is exact`() {
            assertEquals(ZERO, (2 over 3) % (3 over 5))
        }
    }

    @Nested
    inner class Extras {
        @Test
        fun `should compute gcd`() {
            assertEquals(1 over 12, (13 over 6).gcd(3 over 4))
            assertEquals(POSITIVE_INFINITY, POSITIVE_INFINITY.gcd(3 over 4))
            assertEquals(POSITIVE_INFINITY, (3 over 4).gcd(POSITIVE_INFINITY))
            assertEquals(POSITIVE_INFINITY, (3 over 4).gcd(NEGATIVE_INFINITY))
            assertTrue(NaN.gcd(ONE).isNaN(), "NaN does not grok GCD")
            assertTrue(ONE.gcd(NaN).isNaN(), "NaN does not grok GCD")
        }
    }
}

private val Pair<Double, BRat>.primitive get() = first
private val Pair<Double, BRat>.rational get() = second
