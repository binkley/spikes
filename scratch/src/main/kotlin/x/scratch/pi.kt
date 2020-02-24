package x.scratch

import x.scratch.Pi.Companion.generatePiDigits
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

        var j = 1
        while (j < piBuf.size) {
            piBuf[j - 1] = piBuf[j]
            ++j
        }

        piBuf[j - 1] = piIt.next()

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
     * See <a href="https://rosettacode.org/wiki/Pi><cite>Pi - Rosetta Code</cite></a>
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
