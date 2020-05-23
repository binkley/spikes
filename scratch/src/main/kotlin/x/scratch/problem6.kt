package x.scratch

import x.scratch.Problem6.Companion.problem6
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.round
import kotlin.math.sqrt

private const val MAX_N = 50L // Empirically, computable quickly
private const val ROUND_AT = 0.000001 // Rule of thumb for epsilon

fun main() {
    println("a >= b (UPPER DIAGONAL)")
    println("IGNORING TRIVIAL CASES WHERE a OR b IS 0")

    println()
    println("(a,b) -> a²+b²/ab+1 [square²] [log_b(a)]")
    println("----------------------------------------")

    var i = 0L
    var n = 0L
    val frequencies = mutableListOf<Pair<Long, Long>>()
    // TODO: Does Kotlin have a counting map in stdlib?
    val exponents = mutableMapOf<Any, Long>()
    var a = 0L
    loop@ while (true) {
        ++a
        for (b in 1..a) {
            ++i
            val value = problem6(a, b)
            if (value.square) {
                ++n
                val exponent = value.exponent()
                println(
                    "(${value.a},${value.b}) -> ${value.numerator}/${value.denominator} [${value.root()}²] [^$exponent]"
                )

                frequencies += n to i
                exponents.merge(exponent, 1) { old, new ->
                    old + new
                }

                if (n == MAX_N) break@loop
            }
        }
    }

    println()
    println("Nth / CHECKED -> FREQ%")
    println("----------------------")
    frequencies.forEach { (n, i) ->
        println("$n / $i -> ${n.toDouble() * 100 / i}%")
    }

    println()
    println("COUNT -> a=b^EXP")
    println("----------------")

    // Sort by count descending; subsort by exponent descending
    val sortedExponents = exponents.toList()
        .sortedBy { (exp, _) ->
            when {
                exp is Long -> exp.toDouble()
                else -> exp as Double
            }
        }
        .sortedBy { (_, count) -> count }
        .reversed().toMap()

    for ((exp, count) in sortedExponents)
        println("$count -> $exp")
}

/**
 * See https://youtu.be/Y30VF3cSIYQ
 * See https://youtu.be/L0Vj_7Y2-xY
 */
class Problem6 private constructor(val a: Long, val b: Long) {
    val numerator: Long = a * a + b * b
    val denominator: Long = a * b + 1

    init {
        if (integer && !square) error("Theorem is false")
    }

    val integer: Boolean
        get() = 0L == numerator % denominator
    val square: Boolean
        get() {
            if (!integer) return false
            val root = sqrt(numerator.toDouble() / denominator)
            return 0.toDouble() == (root - floor(root))
        }

    fun root() = sqrt(numerator.toDouble() / denominator).toLong()

    fun exponent(): Any {
        val x = if (a == b) 0.0 else log(a.toDouble(), b.toDouble())
        val rounded = round(x)
        return if (x - rounded <= ROUND_AT) rounded.toLong() else x
    }

    companion object {
        fun problem6(a: Long, b: Long) = Problem6(a, b)
    }
}
