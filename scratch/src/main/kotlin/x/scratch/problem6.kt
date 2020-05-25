package x.scratch

import com.github.ajalt.mordant.AnsiCode
import com.github.ajalt.mordant.TermColors
import x.scratch.Problem6.Companion.problem6
import kotlin.math.log
import kotlin.math.round
import kotlin.math.sqrt

/**
 * Empirically, the first 50 are quickly computable before becoming too
 * sparse.
 */
private const val MAX_N = 50L

/**
 * A quick and dirty cutoff for recognizing floating point rounding errors
 * in computing the exponent of "a=b^EXP".
 */
private const val ROUND_AT = 0.000001

fun main() {
    println("a >= b (UPPER DIAGONAL)")
    println("(a,b) → a²+b²⁄ab+1 [square²] [log_b(a)]")
    println("----------------------------------------")

    // Tracking how sparse the squares become
    val sparseness = mutableListOf<Pair<Long, Long>>()
    // Tracking exponents for "a=b^EXP"
    val exponents = mutableListOf<Any>()

    var i = 0L
    var n = 0L
    var a = 0L
    loop@ while (true) {
        ++a
        for (b in 1..a) {
            ++i
            val value = problem6(a, b)
            if (!value.integral) continue // Non-integer result

            ++n
            val exponent = roundIfClose(value.exponent())

            println(value.format(n, exponent))

            sparseness += n to i
            exponents += exponent

            if (n == MAX_N) break@loop
        }
    }

    println()
    println("Nth⁄CHECKED → FREQ%")
    println("--------------------")
    sparseness.forEach { (n, i) ->
        println("$n⁄$i → ${n.toDouble() * 100 / i}%")
    }

    println()
    println("COUNT → a=b^EXP")
    println("---------------")

    // Sort by count descending; sub-sort by exponent descending
    // Casting business is unfortunate to support typing for comparison
    with(TermColors()) {
        exponents
            .map {
                when (it) {
                    is Long -> it.toDouble()
                    else -> it as Double
                }
            }
            .groupingBy { it }.eachCount().toList()
            .sortedBy { (exp, _) -> exp }
            .sortedBy { (_, count) -> count }
            .reversed().forEach { (exponent, count) ->
                val exp = roundIfClose(exponent)
                println("$count → ${exponentColor(exp)(exp.toString())}")
            }
    }
}

/**
 * See https://youtu.be/Y30VF3cSIYQ
 * See https://youtu.be/L0Vj_7Y2-xY
 */
class Problem6 private constructor(val a: Long, val b: Long) {
    val numerator: Long = a * a + b * b
    val denominator: Long = a * b + 1

    /** Checks that this is an integral value. */
    val integral = 0L == numerator % denominator

    /** Returns the square root. */
    fun sqrt() = sqrt(numerator.toDouble() / denominator).toLong()

    /**
     * Returns the integral _or_ floating point exponent `EXP` such that
     * `a=b^EXP`.
     *
     * @return Int or Double
     */
    fun exponent() = if (a == b) 0.0 else log(a.toDouble(), b.toDouble())

    companion object {
        fun problem6(a: Long, b: Long) = Problem6(a, b)
    }
}

private fun Problem6.format(n: Long, exp: Any) = with(TermColors()) {
    val sqrt = sqrt()
    "#$n: ($a,${bColor(b, sqrt)(
        b.toString()
    )}) → $numerator⁄$denominator [${sqrtColor(b, sqrt)(
        sqrt.toString()
    )}²] [^${
    exponentColor(exp)(exp.toString())}]"
}

private fun TermColors.bColor(b: Long, sqrt: Long) =
    when (b) {
        sqrt * sqrt * sqrt -> green
        sqrt -> blue
        else -> reset
    }

private fun TermColors.sqrtColor(b: Long, sqrt: Long) =
    when (b) {
        sqrt * sqrt * sqrt -> green
        sqrt -> blue
        else -> reset
    }

private fun TermColors.exponentColor(exp: Any): AnsiCode {
    if (exp is Double) return yellow // non-integral
    return when (exp as Long) {
        2L -> green // b == sqrt^3
        3L -> blue // b == sqrt
        else -> reset
    }
}

private fun roundIfClose(d: Double): Any {
    val rounded = round(d)
    return if (d - rounded <= ROUND_AT) rounded.toLong() else d
}
