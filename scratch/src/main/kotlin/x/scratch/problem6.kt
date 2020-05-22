package x.scratch

import x.scratch.Problem6.Companion.problem6
import kotlin.math.floor
import kotlin.math.sqrt

fun main() {
    for (a in 0..19)
        for (b in a..19)
            problem6(a, b).apply {
                if (square) println(this)
            }
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

    override fun toString(): String {
        return "Problem6($a,$b) -> $numerator/$denominator ${if (square)
            "[${root()}²]" else ""}"
    }

    companion object {
        fun problem6(a: Int, b: Int) = Problem6(a, b)
    }
}
