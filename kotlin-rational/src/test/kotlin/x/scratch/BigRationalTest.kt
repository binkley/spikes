@file:Suppress("RedundantInnerClassModifier")

package x.scratch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import x.scratch.BigRational.Companion.NEGATIVE_INFINITY
import x.scratch.BigRational.Companion.NEGATIVE_ONE
import x.scratch.BigRational.Companion.NEGATIVE_TEN
import x.scratch.BigRational.Companion.NEGATIVE_TWO
import x.scratch.BigRational.Companion.NaN
import x.scratch.BigRational.Companion.ONE
import x.scratch.BigRational.Companion.POSITIVE_INFINITY
import x.scratch.BigRational.Companion.TEN
import x.scratch.BigRational.Companion.TWO
import x.scratch.BigRational.Companion.ZERO
import java.math.BigDecimal
import kotlin.math.sign

internal class BigRationalTest {
    @Nested
    inner class Conversions {
        @Test
        fun `should round trip from and to big decimal`() {
            for (n in listOf(
                BDouble.TEN,
                BDouble.valueOf(2),
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

            assertThrows<ArithmeticException>("No BigDecimal for +∞") {
                POSITIVE_INFINITY.toBigDecimal()
            }
            assertThrows<ArithmeticException>("No BigDecimal for -∞") {
                NEGATIVE_INFINITY.toBigDecimal()
            }
            assertThrows<ArithmeticException>("No BigDecimal for NaN") {
                NaN.toBigDecimal()
            }
        }

        @Test
        fun `should round trip from and to double`() {
            for (n in listOf(
                10.0,
                2.0,
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
                2.0f,
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
                BInt.TWO,
                BInt.ONE,
                BInt.ZERO,
                -BInt.ONE
            )) assertEquals(
                n,
                n.toBigRational().toBigInteger(),
                "EQ? big integer <-> rational -> $n"
            )

            assertEquals(
                BInt.TWO / BInt.valueOf(3),
                (2 over 3).toBigInteger()
            )
        }

        @Test
        fun `should round trip from and to long`() {
            for (n in listOf(
                10L,
                2L,
                1L,
                0L,
                -1L
            )) assertEquals(
                n,
                n.toBigRational().toLong(),
                "EQ? long <-> rational -> $n"
            )

            assertEquals(
                2L / 3L,
                (2 over 3).toLong()
            )
        }

        @Test
        fun `should round trip from and to int`() {
            for (n in listOf(
                10,
                2,
                1,
                0,
                -1
            )) assertEquals(
                n,
                n.toBigRational().toInt(),
                "EQ? int <-> rational -> $n"
            )

            assertEquals(
                2 / 3,
                (2 over 3).toInt()
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
    inner class FidelityToPrimitives {
        @Test
        fun `should compare as primitives do`() {
            listOf(
                0.0 to ZERO,
                +1.0 to +ONE,
                -1.0 to NEGATIVE_ONE,
                2.0 to TWO,
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
            assertEquals(ZERO, POSITIVE_INFINITY % ONE)
            assertEquals(ZERO, ONE % POSITIVE_INFINITY)
            assertEquals(ZERO, ONE % NEGATIVE_INFINITY)
            assertTrue((NaN % ONE).isNaN())
            assertTrue((ONE % NaN).isNaN())
        }

        @Test
        fun `should increment`() {
            var n = ZERO
            assertEquals(ONE, ++n)
        }

        @Test
        fun `should decrement`() {
            var n = ONE
            assertEquals(ZERO, --n)
        }

        @Test
        fun `should raise`() {
            assertEquals(4 over 9, (2 over 3) `**` 2)
            assertEquals(9 over 4, (2 over 3) `**` -2)
            assertEquals(ONE, (2 over 3) `**` 0)
            assertEquals(POSITIVE_INFINITY, POSITIVE_INFINITY `**` 2)
            assertEquals(POSITIVE_INFINITY, NEGATIVE_INFINITY `**` 2)
            assertEquals(NEGATIVE_INFINITY, NEGATIVE_INFINITY `**` 3)
            assertTrue((NaN `**` 2).isNaN(), "NaN has no powers")
        }

        /** See https://mathworld.wolfram.com/RationalNumber.html */
        @Test
        fun `should obey Hornsberger theorem`() {
            val a = 2 over 3
            val b = 5 over 7
            val c = 11 over 13

            val actual = (((a `**` 2) + (b `**` 2) + (c `**` 2)
                    - a * b - b * c - c * a) /
                    ((a - b) * (b - c) * (c - a))) `**` 2
            val expected = ((a - b) `**` 2).unaryDiv() +
                    ((b - c) `**` 2).unaryDiv() +
                    ((c - a) `**` 2).unaryDiv()

            assertEquals(expected, actual)
        }

        @Test
        fun `should provide quotient and remainder`() {
            listOf(
                3 over 2 to (ONE to (1 over 2)),
                ONE to (ONE to ZERO),
                ZERO to (ZERO to ZERO),
                NEGATIVE_ONE to (NEGATIVE_ONE to ZERO),
                -3 over 2 to (NEGATIVE_ONE to (1 over 2)),
                POSITIVE_INFINITY to (POSITIVE_INFINITY to ZERO),
                NEGATIVE_INFINITY to (NEGATIVE_INFINITY to ZERO)
            )

            val (quotientA, remainderA) = NaN.divideAndRemainder(ONE)
            assertTrue(quotientA.isNaN(), "NaN does not have a quotient")
            assertTrue(remainderA.isNaN(), "NaN does not have a remainder")

            val (quotientB, remainderB) = NaN.divideAndRemainder(NaN)
            assertTrue(quotientB.isNaN(), "NaN does not have a quotient")
            assertTrue(remainderB.isNaN(), "NaN does not have a remainder")
        }

        @Test
        fun `should respect the continuum`() {
            assertEquals(POSITIVE_INFINITY, POSITIVE_INFINITY + ONE)
            assertEquals(POSITIVE_INFINITY, POSITIVE_INFINITY - ONE)
            assertEquals(POSITIVE_INFINITY, POSITIVE_INFINITY * TWO)
            assertEquals(POSITIVE_INFINITY, POSITIVE_INFINITY / TWO)
            assertEquals(NEGATIVE_INFINITY, NEGATIVE_INFINITY + ONE)
            assertEquals(NEGATIVE_INFINITY, NEGATIVE_INFINITY - ONE)
            assertEquals(NEGATIVE_INFINITY, NEGATIVE_INFINITY * TWO)
            assertEquals(NEGATIVE_INFINITY, NEGATIVE_INFINITY / TWO)
        }

        @Test
        fun `should treat negation of non-finite values as primitive do`() {
            assertEquals(NEGATIVE_INFINITY, -POSITIVE_INFINITY)
            assertEquals(POSITIVE_INFINITY, -NEGATIVE_INFINITY)
            assertSame(NaN, -NaN)
        }
    }

    @Nested
    inner class Strings {
        @Test
        fun `should print nicely`() {
            assertEquals("0", "$ZERO")
            assertEquals("1", "$ONE")
            assertEquals("-1", "${NEGATIVE_ONE}")
            assertEquals("1/10", "${1 over 10}")
            assertEquals("-1/10", "${-1 over 10}")
        }

        @Test
        fun `should print as primitives do`() {
            assertEquals("${Double.POSITIVE_INFINITY}", "$POSITIVE_INFINITY")
            assertEquals("${Double.NEGATIVE_INFINITY}", "$NEGATIVE_INFINITY")
            assertEquals("${Double.NaN}", "$NaN")
        }
    }

    @Nested
    inner class Functions {
        @Test
        fun `should test if an integer`() {
            assertTrue(ZERO.isInteger(), "Zero is an integer")
            assertFalse((3 over 2).isInteger(), "3/2 is not an integer")
            assertFalse(POSITIVE_INFINITY.isInteger(), "+∞ is not an integer")
            assertFalse(NEGATIVE_INFINITY.isInteger(), "-∞ is not an integer")
            assertFalse(NaN.isInteger(), "NaN is not an integer")
        }

        @Test
        fun `should compute gcd`() {
            assertEquals(1 over 12, (13 over 6).gcd(3 over 4))
            assertEquals(POSITIVE_INFINITY, POSITIVE_INFINITY.gcd(3 over 4))
            assertEquals(POSITIVE_INFINITY, (3 over 4).gcd(POSITIVE_INFINITY))
            assertEquals(POSITIVE_INFINITY, (3 over 4).gcd(NEGATIVE_INFINITY))
            assertTrue(NaN.gcd(ONE).isNaN(), "NaN does not grok GCD")
            assertTrue(ONE.gcd(NaN).isNaN(), "NaN does not grok GCD")
        }

        @Test
        fun `should compute mediant`() {
            assertEquals(5 over 7, (2 over 3).mediant(3 over 4))
            listOf(
                ZERO to ONE,
                ONE to TWO,
                NEGATIVE_ONE to ZERO,
                POSITIVE_INFINITY to POSITIVE_INFINITY,
                NEGATIVE_INFINITY to ZERO
            ).forEach { (other, expected) ->
                assertEquals(expected, POSITIVE_INFINITY.mediant(other))
            }
            listOf(
                ZERO to NEGATIVE_ONE,
                ONE to ZERO,
                NEGATIVE_ONE to NEGATIVE_TWO,
                POSITIVE_INFINITY to ZERO,
                NEGATIVE_INFINITY to NEGATIVE_INFINITY
            ).forEach { (other, expected) ->
                assertEquals(expected, NEGATIVE_INFINITY.mediant(other))
            }
            listOf(
                ZERO,
                ONE,
                NEGATIVE_ONE,
                POSITIVE_INFINITY,
                NEGATIVE_INFINITY,
                NaN
            ).forEach { other ->
                assertTrue(
                    NaN.mediant(other).isNaN(),
                    "Mediants including NaN are NaN"
                )
                assertTrue(
                    other.mediant(NaN).isNaN(),
                    "Mediants including NaN are NaN"
                )
            }
        }
    }

    @Nested
    inner class Rounding {
        @Test
        fun `should round towards zero`() {
            listOf(
                3 over 2 to ONE,
                ONE to ONE,
                ZERO to ZERO,
                NEGATIVE_ONE to NEGATIVE_ONE,
                -3 over 2 to NEGATIVE_ONE,
                POSITIVE_INFINITY to POSITIVE_INFINITY,
                NEGATIVE_INFINITY to NEGATIVE_INFINITY
            ).forEach { (value, expected) ->
                assertEquals(
                    expected,
                    value.truncate(),
                    "$value rounds towards zero ($expected)"
                )
            }

            assertTrue(NaN.truncate().isNaN(), "NaN does not round")
        }

        @Test
        fun `should round towards floor`() {
            listOf(
                3 over 2 to ONE,
                ONE to ONE,
                ZERO to ZERO,
                NEGATIVE_ONE to NEGATIVE_ONE,
                -3 over 2 to -TWO,
                POSITIVE_INFINITY to POSITIVE_INFINITY,
                NEGATIVE_INFINITY to NEGATIVE_INFINITY
            ).forEach { (value, expected) ->
                assertEquals(
                    expected,
                    value.floor(),
                    "$value rounds towards floor ($expected)"
                )
            }

            assertTrue(NaN.floor().isNaN(), "NaN does not round")
        }

        @Test
        fun `should round towards ceiling`() {
            listOf(
                3 over 2 to TWO,
                ONE to ONE,
                ZERO to ZERO,
                NEGATIVE_ONE to NEGATIVE_ONE,
                -3 over 2 to NEGATIVE_ONE,
                POSITIVE_INFINITY to POSITIVE_INFINITY,
                NEGATIVE_INFINITY to NEGATIVE_INFINITY
            ).forEach { (value, expected) ->
                assertEquals(
                    expected,
                    value.ceil(),
                    "$value rounds towards ceiling ($expected)"
                )
            }

            assertTrue(NaN.ceil().isNaN(), "NaN does not round")
        }
    }

    @Nested
    inner class Properties {
        @Test
        fun `should have a sign`() {
            listOf(
                -2.0 to -TWO,
                -0.5 to (-1 over 2),
                0.0 to ZERO,
                0.5 to (1 over 2),
                2.0 to TWO,
                Double.POSITIVE_INFINITY to POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY to NEGATIVE_INFINITY
            ).forEach { (p, r) ->
                assertEquals(
                    p.sign,
                    r.signum().toDouble(),
                    "Rational $r is as primitive $p for signum"
                )
            }

            assertTrue(NaN.sign.isNaN(), "NaN does not have a sign")
        }

        @Test
        fun `should have an absolute value`() {
            assertEquals(ONE, ONE.absoluteValue)
            assertEquals(ZERO, ZERO.absoluteValue)
            assertEquals(ONE, NEGATIVE_ONE.absoluteValue)
            assertTrue(
                NaN.absoluteValue.isNaN(),
                "NaN does not have an absolute value"
            )
        }

        @Test
        fun `should have a characteristic`() {
            assertEquals(ZERO, BRat.characteristic)
        }

        @Test
        fun `should reciprocate`() {
            assertEquals(3 over 2, (2 over 3).reciprocal)
            assertEquals(
                ZERO,
                POSITIVE_INFINITY.reciprocal,
                "Inverse of +∞ is 0"
            )
            assertEquals(
                ZERO,
                NEGATIVE_INFINITY.reciprocal,
                "Inverse of -∞ is 0"
            )
            assertTrue(
                NaN.reciprocal.isNaN(),
                "Inverse of NaN remains NaN"
            )
        }
    }

    @Nested
    inner class OddsAndEnds {
        @Test
        fun `should normalize`() {
            assertEquals(-2 over 3, 6 over -9)
            assertEquals(1 over -1, NEGATIVE_ONE, "Negative denominator")
            assertEquals(POSITIVE_INFINITY, 1_000_000 over 0)
            assertEquals(NEGATIVE_INFINITY, -1_000_000 over 0)
            assertTrue((0 over 0).isNaN(), "Division by zero")
        }

        @Test
        fun `should use constants`() {
            assertSame(ZERO, 0 over 1, "Zero is a constant")
            assertSame(ONE, 1 over 1, "One is a constant")
            assertSame(NEGATIVE_ONE, -1 over 1, "Negative one is a constant")
            assertSame(NEGATIVE_ONE, -ONE, "Negating one is a constant")
            assertSame(
                ONE, -NEGATIVE_ONE, "Negating negative one is a constant"
            )
            assertSame(TWO, 2 over 1, "Two is a constant")
            assertSame(NEGATIVE_TWO, -2 over 1, "Negative two is a constant")
            assertSame(NEGATIVE_TWO, -TWO, "Negating two is a constant")
            assertSame(
                TWO, -NEGATIVE_TWO, "Negating negative two is a constant"
            )
            assertSame(TEN, 10 over 1, "Ten is a constant")
            assertSame(NEGATIVE_TEN, -10 over 1, "Negative ten is a constant")
            assertSame(NEGATIVE_TEN, -TEN, "Negating ten is a constant")
            assertSame(
                TEN, -NEGATIVE_TEN, "Negating negative ten is a constant"
            )
            assertSame(POSITIVE_INFINITY, 2 over 0, "Infinity is a constant")
            assertSame(
                POSITIVE_INFINITY, -NEGATIVE_INFINITY,
                "Negating negative infinity is a constant"
            )
            assertSame(
                NEGATIVE_INFINITY, -2 over 0,
                "Negative infinity is a constant"
            )
            assertSame(
                NEGATIVE_INFINITY, -POSITIVE_INFINITY,
                "Negating infinity is a constant"
            )
            assertSame(NaN, 0 over 0, "NaN is a constant")
            assertSame(NaN, -NaN, "Negating NaN is a constant")
            assertNotSame(
                3 over 1, 3 over 1, "Non-constants are separate objects"
            )
        }

        @Test
        fun `should reduce to lowest terms`() {
            assertEquals(
                21 over 20,
                BigDecimal.valueOf(105, 2).toBigRational()
            )
            assertEquals(3 over 1, 6 over 2)
        }

        @Test
        fun `should understand equality`() {
            listOf(
                3 over 2,
                ONE,
                ZERO,
                NEGATIVE_ONE,
                -3 over 2,
                POSITIVE_INFINITY,
                NEGATIVE_INFINITY,
                NaN
            ).cartesian().forEach { (a, b) ->
                when {
                    a.isNaN() || b.isNaN() -> assertNotEquals(a, b)
                    a === b -> assertEquals(a, b)
                    else -> assertNotEquals(a, b)
                }
            }
        }

        @Test
        fun `should hash`() {
            listOf(ZERO, ONE, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN)
                .cartesian().forEach { (a, b) ->
                    if (a == b || a.isNaN() && b.isNaN()) assertEquals(
                        a.hashCode(),
                        b.hashCode(),
                        "Hash codes of $a and $b"
                    )
                    else assertNotEquals(
                        a.hashCode(),
                        b.hashCode(),
                        "Hash codes of $a and $b"
                    )
                }
        }
    }
}

private val Pair<Double, BRat>.primitive get() = first
private val Pair<Double, BRat>.rational get() = second
