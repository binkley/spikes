package x.scratch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import x.scratch.BigRational.Companion.ONE
import x.scratch.BigRational.Companion.TEN
import x.scratch.BigRational.Companion.TWO
import x.scratch.BigRational.Companion.ZERO

private val THREE = BInt.valueOf(3)
private val FOUR = BInt.valueOf(4)
private val TWELVE = BInt.valueOf(12)

internal class ContinuedFractionTest {
    @Test
    fun `should be a JDK list`() {
        val two = BInt.TWO
        val ten = BInt.TEN

        val c = (145 over 7).toContinuedFraction()

        println("C -> $c")

        assertTrue(c.contains(two), "Missing parts: 2")
        assertFalse(c.contains(ten), "Unexpected part: 10")
        // TODO: For JaCoCo coverage only -- Kotlin strongly dislikes
        assertFalse(c.contains(this))
        assertEquals(2, c.indexOf(two), "First part 2 in the wrong place")
        assertEquals(-1, c.indexOf(ten), "10 should not be a part")
        // TODO: For JaCoCo coverage only -- Kotlin strongly dislikes
        assertEquals(-1, c.indexOf(this))
        assertEquals(3, c.lastIndexOf(two), "Last part 2 in the wrong place")
        assertEquals(-1, c.lastIndexOf(ten), "10 should not be a part")
        // TODO: For JaCoCo coverage only -- Kotlin strongly dislikes
        assertEquals(-1, c.lastIndexOf(this))
    }

    @Test
    fun `should round trip continued fractions`() {
        val cfA = (3245 over 1000).toContinuedFraction()
        assertEquals(
            listOf(
                THREE,
                FOUR,
                TWELVE,
                FOUR
            ),
            cfA
        )
        assertEquals((3245 over 1000), cfA.toBigRational())
        val negCfA = (-3245 over 1000).toContinuedFraction()
        assertEquals(
            listOf(
                -FOUR,
                BInt.ONE,
                THREE,
                TWELVE,
                FOUR
            ),
            negCfA as List<BInt>
        )
        assertEquals((-3245 over 1000), negCfA.toBigRational())
        assertEquals(
            listOf(BInt.ZERO),
            ZERO.toContinuedFraction()
        )
        assertEquals(
            listOf(BInt.ONE),
            ONE.toContinuedFraction()
        )
        assertEquals(
            listOf(BInt.ZERO, THREE),
            (1 over 3).toContinuedFraction()
        )
    }

    @Test
    fun `should have parts`() {
        val cfA = (3245 over 1000).toContinuedFraction()
        assertEquals(THREE, cfA.integerPart)
        assertEquals(
            listOf(
                FOUR,
                TWELVE,
                FOUR
            ),
            cfA.fractionalParts
        )
        val negCfA = (-3245 over 1000).toContinuedFraction()
        assertEquals(BInt.valueOf(-4), negCfA.integerPart)
        assertEquals(
            listOf(
                BInt.ONE,
                THREE,
                TWELVE,
                FOUR
            ),
            negCfA.fractionalParts
        )
    }

    @Test
    fun `should invert`() {
        assertEquals(
            listOf(
                BInt.TWO
            ),
            (1 over 2).toContinuedFraction().unaryDiv()
        )
        assertEquals(
            listOf(
                BInt.ZERO,
                THREE,
                FOUR,
                TWELVE,
                FOUR
            ),
            (3245 over 1000).toContinuedFraction().unaryDiv()
        )
        assertEquals(
            listOf(
                BInt.ZERO,
                -FOUR,
                BInt.ONE,
                THREE,
                TWELVE,
                FOUR
            ),
            (-3245 over 1000).toContinuedFraction().reciprocal
        )
    }

    @Test
    fun `should pretty print`() {
        assertEquals(
            "[3; 4, 12, 4]",
            (3245 over 1000).toContinuedFraction().toString()
        )
        assertEquals(
            "[2;]",
            TWO.toContinuedFraction().toString()
        )
    }
}
