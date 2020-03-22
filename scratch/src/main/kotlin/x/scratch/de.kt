package x.scratch

import x.scratch.Run.HOT_COLD
import x.scratch.Run.ONCE
import x.scratch.Run.TRIALS
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.system.exitProcess

private val run = HOT_COLD
private const val graph = true
private const val n = 20
private const val max = 9
private const val cutoff = 10000
private const val trials = 100

private enum class Run {
    ONCE,
    TRIALS,
    HOT_COLD
}

fun main() {
    when (run) {
        ONCE -> printSummary(runOnce(randomInit(), printStep()))
        TRIALS -> runTrials()
        HOT_COLD -> printSummary(runOnce(hotColdInit(), printStep()))
    }
}

private fun printStep(): (Int, List<Int>) -> Unit {
    return { i, last ->
        if (graph) {
            println("$i:")
            last.forEach {
                print('|')
                println("-".repeat(it))
            }
        } else
            println("$i: $last")
    }
}

private fun printSummary(result: RunResult) {
    val (stepsNeeded, initAverage, equilibrium) = result
    println("$stepsNeeded STEPS NEEDED")
    println("$initAverage INITIAL AVERAGE")
    println("$equilibrium EQUILIBRIUM")
}

typealias Progress = (Int, List<Int>) -> Unit

private data class RunResult(
    val stepsNeeded: Int,
    val initAverage: Int,
    val equilibrium: Int
)

private fun runOnce(init: List<Int>, progress: Progress): RunResult {
    val initAverage = init.average()
    var last: List<Int> = init
    var i = 0

    progress(i, last)
    while (!last.equilibrium()) {
        ++i
        avoidRunningAway(i)
        last = last.next()
        progress(i, last)
    }
    val equilibrium = last[0]

    return RunResult(i, initAverage, equilibrium)
}

private fun runTrials() {
    val counts = mutableListOf(0, 0, 0)
    repeat(trials) {
        val (_, initAverage, equilibrium) = runOnce(randomInit()) { _, _ -> }
        when {
            initAverage < equilibrium -> ++counts[0]
            initAverage == equilibrium -> ++counts[1]
            initAverage > equilibrium -> ++counts[2]
        }
    }
    println("ROSE: ${counts[0]}, STAYED: ${counts[1]}, FELL: ${counts[2]}")
}

private fun avoidRunningAway(i: Int) {
    if (i >= cutoff) {
        println("MORE THAN $cutoff STEPS NEEDED")
        exitProcess(1)
    }
}

private fun randomInit(): List<Int> {
    val init = ArrayList<Int>(n)
    repeat(n) {
        init.add(Random.nextInt(0, max + 1).absoluteValue)
    }
    return init
}

private fun hotColdInit(): List<Int> {
    val init = ArrayList<Int>(n)
    repeat(n / 2) {
        init.add(9)
    }
    repeat(n / 2 + n % 2) {
        init.add(0)
    }
    return init
}

private fun List<Int>.equilibrium(): Boolean {
    val first = first()
    (1 until size).forEach {
        if (this[it] != first) return false
    }
    return true
}

private fun List<Int>.average() = middle(*(this.toIntArray()))

private fun List<Int>.next(): List<Int> {
    val updated = ArrayList<Int>(size)
    updated.add(middle(first(), this[1]))
    (1 until size - 1).forEach {
        updated.add(middle(this[it - 1], this[it], this[it + 1]))
    }
    updated.add(middle(last(), this[size - 2]))
    return updated
}

private fun middle(vararg xs: Int): Int {
    infix fun Int.outOf(base: Int) =
        if (Random.nextInt(0, base) < this) 1 else 0

    val sum = xs.sum()
    return sum / xs.size + (sum % xs.size outOf xs.size)
}

private fun adjusted(left: Int, middle: Int, right: Int): Int {
    // TODO: This seems to capture https://youtu.be/ly4S0oi3Yz8?t=605
    //  but does not
    val upperDiff = right - middle
    val lowerDiff = middle - left
    val doubleDiff = upperDiff - lowerDiff

    return middle + when (doubleDiff % 2) {
        0 -> doubleDiff / 2
        else -> doubleDiff / 2 + Random.nextInt(0, 2)
    }
}
