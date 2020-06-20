package x.scratch

import x.scratch.BigRational.Companion.NEGATIVE_INFINITY
import x.scratch.BigRational.Companion.NaN
import x.scratch.BigRational.Companion.POSITIVE_INFINITY
import java.math.BigInteger.TWO

fun main() {
    println("== FLOATING POINT")

    fun header(text: String) {
        println()
        println(text)
    }

    header("PRELIMINARIES")

    println(
        "-(Infinity) == -Infinity? ->" +
                " ${-Double.POSITIVE_INFINITY == Double.NEGATIVE_INFINITY}"
    )
    println(
        "-(-Infinity) == Infinity? ->" +
                " ${-Double.NEGATIVE_INFINITY == Double.POSITIVE_INFINITY}"
    )

    listOf(
        Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN
    ).printRelations()

    header("FOO-AND-REMAINDER FUNCTIONS")

    println("√2 -> ${TWO.sqrtAndRemainder()!!.contentToString()}")

    header("RATIOS OF BIG RATIO NON-FINITE VALUES")

    listOf(POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN).printRelations()

    header("COMPARE PRIMITIVES TO RATIOS")

    println("+∞ COMPARED")
    println(
        "EQ? ${(Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY)
                == (POSITIVE_INFINITY == POSITIVE_INFINITY)}"
    )
    println(
        "LT? ${(Double.POSITIVE_INFINITY < Double.POSITIVE_INFINITY)
                == (POSITIVE_INFINITY < POSITIVE_INFINITY)}"
    )
    println(
        "GT? ${(Double.POSITIVE_INFINITY > Double.POSITIVE_INFINITY)
                == (POSITIVE_INFINITY > POSITIVE_INFINITY)}"
    )
    println("-∞ COMPARED")
    println(
        "EQ? ${(Double.NEGATIVE_INFINITY == Double.NEGATIVE_INFINITY)
                == (NEGATIVE_INFINITY == NEGATIVE_INFINITY)}"
    )
    println(
        "LT? ${(Double.NEGATIVE_INFINITY < Double.NEGATIVE_INFINITY)
                == (NEGATIVE_INFINITY < NEGATIVE_INFINITY)}"
    )
    println(
        "GT? ${(Double.NEGATIVE_INFINITY > Double.NEGATIVE_INFINITY)
                == (NEGATIVE_INFINITY > NEGATIVE_INFINITY)}"
    )
    println("NaN COMPARED")
    println("EQ? ${(Double.NaN == Double.NaN) == (NaN == NaN)}")
    println("LT? ${(Double.NaN < Double.NaN) == (NaN < NaN)}")
    println("GT? ${(Double.NaN > Double.NaN) == (NaN > NaN)}")

    header("RATIOS OF DOUBLES")

    for (d in listOf(
        10.0,
        1.0,
        0.0,
        0.1,
        0.01,
        0.1 + 0.2,
        2.0 / 3.0,
        -1.0,
        -0.1,
        -0.0,
        Double.MAX_VALUE,
        -Double.MAX_VALUE,
        Double.MIN_VALUE,
        -Double.MIN_VALUE,
        Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY,
        Double.NaN
    ))
        printRoundTrip(d)

    header("RATIOS OF FLOATS")

    for (d in listOf(
        10.0f,
        1.0f,
        0.0f,
        0.1f,
        0.01f,
        0.1f + 0.2f,
        2.0f / 3.0f,
        -1.0f,
        -0.1f,
        -0.0f,
        Float.MAX_VALUE,
        -Float.MAX_VALUE,
        Float.MIN_VALUE,
        -Float.MIN_VALUE,
        Float.POSITIVE_INFINITY,
        Float.NEGATIVE_INFINITY,
        Float.NaN
    ))
        printRoundTrip(d)
}

internal fun <T> List<T>.cartesian() =
    flatMap { outer -> map { inner -> outer to inner } }

private fun <T : Comparable<T>> List<T>.printRelations() =
    cartesian().forEach { (a, b) ->
        val eq = a == b
        val lt = a < b
        val gt = a > b
        val count = listOf(eq, lt, gt).filter { it }.count()

        println("TRUE JUST ONCE? -> $count for $a vs $b")
        println("- EQ? $a $b -> $eq")
        println("- LT? $a $b -> $lt")
        println("- GT? $a $b -> $gt")
    }

private fun printRoundTrip(floatingPoint: Double) {
    val ratio = floatingPoint.toBigRational()
    val backAgain = ratio.toDouble()

    println("${floatingPoint.print} -> $ratio -> ${backAgain.print}")

    if (floatingPoint eq backAgain) return

    error("DID NOT ROUND TRIP: $floatingPoint")
}

private fun printRoundTrip(floatingPoint: Float) {
    val ratio = floatingPoint.toBigRational()
    val backAgain = ratio.toFloat()

    println("${floatingPoint.print} -> $ratio -> ${backAgain.print}")

    if (floatingPoint eq backAgain) return

    error("DID NOT ROUND TRIP: $floatingPoint")
}

private val Double.print
    get() = when (this) {
        Double.MAX_VALUE -> "MAX_VALUE"
        -Double.MAX_VALUE -> "-MAX_VALUE"
        Double.MIN_VALUE -> "MIN_VALUE"
        -Double.MIN_VALUE -> "-MIN_VALUE"
        else -> toString()
    }

internal infix fun Double.eq(other: Double) =
    this == other || isNaN() && other.isNaN()

private val Float.print
    get() = when (this) {
        Float.MAX_VALUE -> "MAX_VALUE"
        -Float.MAX_VALUE -> "-MAX_VALUE"
        Float.MIN_VALUE -> "MIN_VALUE"
        -Float.MIN_VALUE -> "-MIN_VALUE"
        else -> toString()
    }

internal infix fun Float.eq(other: Float) =
    this == other || this.isNaN() && other.isNaN()
