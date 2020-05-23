package x.scratch

import x.scratch.Problem6.Companion.problem6
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.round
import kotlin.math.sqrt

private const val MAX_N = 50 // Empirically, computable quickly
private const val ROUND_AT = 0.000001 // Rule of thumb for epsilon

fun main() {
    println("IGNORE TRIVIAL CASES WHERE a OR b IS 0")
    println("(a,b) -> a²+b²/ab+1 [square²][log_b(a)] -> n/total")
    println("--------------------------------------------------")

    var i = 0
    var n = 0
    // TODO: Does Kotlin have a counting map in stdlib?
    val stats = mutableMapOf<Any, Int>()
    var a = 0
    loop@ while (true) {
        ++a
        for (b in 1..a) {
            ++i
            val value = problem6(a, b)
            if (value.square) {
                ++n
                println("$value -> $n / $i")

                stats.merge(value.normalizedExponent(), 1) { old, new ->
                    old + new
                }

                if (n == MAX_N) break@loop
            }
        }
    }
    println()
    println("count -> b=a^EXP")
    println("----------------")

    // Sort by count descending; subsort by exponent descending
    val sortedStats = stats.toList()
        .sortedBy { (exp, _) ->
            when {
                exp is Int -> exp.toDouble()
                else -> exp as Double
            }
        }
        .sortedBy { (_, count) -> count }
        .reversed().toMap()

    for ((exp, count) in sortedStats)
        println("$count -> $exp")
}

/**
 * See https://youtu.be/Y30VF3cSIYQ
 * See https://youtu.be/L0Vj_7Y2-xY
 */
class Problem6 private constructor(val a: Int, val b: Int) {
    private val numerator: Int = a * a + b * b
    private val denominator: Int = a * b + 1

    init {
        if (integer && !square) error("Theorem is false")
    }

    val integer: Boolean
        get() = 0 == numerator % denominator
    val square: Boolean
        get() {
            if (!integer) return false
            val root = sqrt(numerator.toDouble() / denominator)
            return 0.toDouble() == (root - floor(root))
        }

    private fun root() = sqrt(numerator.toDouble() / denominator).toInt()
    private fun exponent() =
        if (a == b) 0.0 else log(a.toDouble(), b.toDouble())

    fun normalizedExponent(): Any {
        val x = exponent()
        val rounded = round(x)
        return if (x - rounded <= ROUND_AT) rounded.toInt() else x
    }

    override fun toString() =
        "($a,$b) -> $numerator/$denominator ${
        if (square) "[${root()}²]" else ""}[^${normalizedExponent()}]"

    companion object {
        fun problem6(a: Int, b: Int) = Problem6(a, b)
    }
}
