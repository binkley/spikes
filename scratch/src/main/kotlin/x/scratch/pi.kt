package x.scratch

import x.scratch.Pi.Companion.generatePiDigits
import java.lang.System.arraycopy
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.TEN
import java.math.BigInteger.TWO
import java.math.BigInteger.ZERO

fun main() {
    val name = "BKO" // TODO: Ố in Oxley, ie, UNICODE
    println(name)
    println(findInPi(name))
}

/**
 * Finds the first 0-based index into the digits of PI corresponding to the
 * 0-based position in the plain Latin alphabet of a string of characters.
 * For those characters with positions 10 and greater, they are treated as two
 * separate digits to locate in PI.  Examples:
 *
 * - "A" is looking for the digit string "0", found in position 32
 * - "Z" is looking for the digit string "25" (a 2 followed by a 5), found in
 * position 89
 *
 * It is believed every possible string eventually appears as a digit string
 * within PI, however finding the position may exhaust your patience, and some
 * strings will not be found before the heat death of the Universe.
 */
fun findInPi(str: String): Int {
    val digits = toDigits(str)

    val piBuf = IntArray(digits.size)
    val pi = generatePiDigits()
    val piIt = pi.iterator()
    for (i in digits.indices) {
        piBuf[i] = piIt.next()
    }

    var i = 0
    while (true) {
        if (digits.contentEquals(piBuf)) return i

        // Shift contents left by 1, and fill in the end with next digit
        arraycopy(piBuf, 1, piBuf, 0, piBuf.size - 1)
        piBuf[piBuf.size - 1] = piIt.next()

        ++i
    }
}

private fun toDigits(char: Int): Array<Int> {
    return if (char < 10) arrayOf(char)
    else arrayOf(char / 10, char % 10)
}

private const val Apos = 'A'.toInt()
private fun toDigits(str: String): IntArray {
    val array = IntArray(str.length * 2)
    var j = 0
    str.forEach { ch ->
        val c = ch.toUpperCase()
        val i = c.toInt() - Apos
        toDigits(i).forEach {
            array[j++] = it
        }
    }
    return array.sliceArray(0 until j)
}

private val THREE: BigInteger = BigInteger.valueOf(3)
private val FOUR: BigInteger = BigInteger.valueOf(4)
private val SEVEN: BigInteger = BigInteger.valueOf(7)

class Pi : Iterator<Int> {
    companion object {
        fun generatePiDigits(): Sequence<Int> {
            return object : Sequence<Int> {
                override fun iterator(): Iterator<Int> {
                    return Pi()
                }
            }
        }
    }

    private var q: BigInteger = ONE
    private var r: BigInteger = ZERO
    private var t: BigInteger = ONE
    private var k: BigInteger = ONE
    private var n: BigInteger = THREE
    private var l: BigInteger = THREE

    override fun hasNext() = true

    override fun next() = nextDigitOfPi()

    /**
     * See <a href="https://rosettacode.org/wiki/Pi"><cite>Pi - Rosetta Code</cite></a>
     */
    private fun nextDigitOfPi(): Int {
        var nn: BigInteger
        var nr: BigInteger

        while (true) {
            val foo = FOUR * q + r - t
            val bar = n * t

            if (foo < bar) {
                val nextDigit = n
                nr = TEN * (r - bar)
                n = (TEN * (THREE * q + r)) / t - TEN * n
                q *= TEN
                r = nr
                return nextDigit.toInt()
            } else {
                nr = (TWO * q + r) * l
                nn = (q * (SEVEN * k) + TWO + r * l) / (t * l)
                q *= k
                t *= l
                l += TWO
                k += ONE
                n = nn
                r = nr
            }
        }
    }
}
