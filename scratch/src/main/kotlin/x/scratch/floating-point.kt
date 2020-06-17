package x.scratch

import kotlin.Double.Companion.NaN

fun main() {
    println("== FLOATING POINT")

    println()
    println("FP 0.1 + 0.2 -> ${0.1 + 0.2}")

    println()
    println("NAN")
    @Suppress("ConvertNaNEquality")
    println("NaN == NaN -> ${NaN == NaN}")
    println("NaN.isNan -> ${NaN.isNaN()}")
}
