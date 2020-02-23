package x.scratch

import x.scratch.Pi.Companion.generatePiDigits
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.TEN
import java.math.BigInteger.TWO
import java.math.BigInteger.ZERO
import java.util.Objects.hash

fun main() {
    val name = "BrianKeithOxley"
    println(name)
    val nameDigits = toDigits(name)
    val nameAsDigits = nameDigits.joinToString("")
    println(nameAsDigits)

    val pi = generatePiDigits()
    pi.take(nameAsDigits.length).forEach {
        print(it)
    }
    println()

    println(WindowOnPi(name))
}

data class WindowOnPi(val str: String) {
    private val digits = toDigits(str)
    private val pi = generatePiDigits()

    override fun equals(other: Any?) = this === other ||
            other is WindowOnPi &&
            str == other.str

    override fun hashCode() = hash(str)

    override fun toString() = "${this.javaClass.simpleName}[$str]"
}

private const val Apos = 'A'.toInt()
fun toDigits(str: String) = str.map {
    val c = it.toUpperCase()
    c.toInt() - Apos
}.toIntArray()

fun findInPi(str: String) {
    val digitsToFind = toDigits(str)

    println(str)
    str.map {
        it.toUpperCase()
    }.forEach {
        print(' ')
        print(it.toInt() - 'A'.toInt())
    }
    println()
}

val THREE: BigInteger = BigInteger.valueOf(3)
val FOUR: BigInteger = BigInteger.valueOf(4)
val SEVEN: BigInteger = BigInteger.valueOf(7)

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

    override fun next() = digitsOfPi()

    /**
     * See <a href="https://rosettacode.org/wiki/Pi><cite>Pi - Rosetta Code</cite></a>
     */
    fun digitsOfPi(): Int {
        var nn: BigInteger
        var nr: BigInteger
        var i = 0L

        while (true) {
            val foo = FOUR * q + r - t
            val bar = n * t

            if (foo < bar) {
                val printMe = n
                nr = TEN * (r - bar)
                n = (TEN * (THREE * q + r)) / t - TEN * n
                q *= TEN
                r = nr
                return printMe.toInt()
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
