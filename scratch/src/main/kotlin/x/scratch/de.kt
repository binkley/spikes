package x.scratch

import x.scratch.Run.MANY_BELL_CURVE
import x.scratch.Run.ONCE_HOT_COLD
import x.scratch.Run.ONCE_RANDOM
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.system.exitProcess

private val run = ONCE_RANDOM
private const val graph = true
private const val n = 20
private const val max = 9
private const val cutoff = 10000
private const val trials = 100

private enum class Run {
    ONCE_RANDOM,
    ONCE_HOT_COLD,
    MANY_BELL_CURVE
}

fun main() {
    when (run) {
        ONCE_RANDOM -> printSummary(runOnce(randomInit(), printStep()))
        ONCE_HOT_COLD -> printSummary(runOnce(hotColdInit(), printStep()))
        MANY_BELL_CURVE -> runTrials()
    }
}

typealias StepProgress = (Int, List<Int>) -> Unit

private data class RunResult(
    val stepsNeeded: Int,
    val initAverage: Int,
    val equilibrium: Int
)

private fun runOnce(init: List<Int>, progress: StepProgress): RunResult {
    val initAverage = init.average()
    var last: List<Int> = init
    var nSteps = 0

    progress(nSteps, last)
    while (!last.equilibrium()) {
        ++nSteps
        avoidRunningAway(nSteps)
        last = last.next()
        progress(nSteps, last)
    }
    val equilibrium = last[0]

    return RunResult(nSteps, initAverage, equilibrium)
}

private fun runTrials() {
    var rose = 0
    var stayed = 0
    var fell = 0
    repeat(trials) {
        val (_, initAverage, equilibrium) = runOnce(randomInit()) { _, _ -> }
        when {
            initAverage < equilibrium -> ++rose
            initAverage == equilibrium -> ++stayed
            initAverage > equilibrium -> ++fell
        }
    }
    println("ROSE: $rose, STAYED: $stayed, FELL: $fell")
}

private fun avoidRunningAway(nSteps: Int) {
    if (nSteps >= cutoff) {
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

private fun printStep(): (Int, List<Int>) -> Unit {
    return { nSteps, stepValues ->
        if (graph) {
            println("$nSteps:")
            graphDifferences(stepValues)
        } else
            println("$nSteps: $stepValues")
    }
}

private fun printSummary(result: RunResult) {
    val (nSteps, initAverage, equilibrium) = result
    println("$nSteps STEPS NEEDED")
    println("$initAverage INITIAL AVERAGE")
    println("$equilibrium EQUILIBRIUM")
}
