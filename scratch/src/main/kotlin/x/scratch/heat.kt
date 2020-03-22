package x.scratch

import x.scratch.Run.MANY_BELL_CURVE
import x.scratch.Run.ONCE_HOT_COLD
import x.scratch.Run.ONCE_RANDOM
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.system.exitProcess

private val run = MANY_BELL_CURVE
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

/**
 * Modelling numerical approximation of the heat equation with integers.
 * Important note: This does not use a correct adjustment to each point as it
 * steps through time: it uses a simple 3-point average.
 */
fun main() {
    when (run) {
        ONCE_RANDOM -> printSummary(runOnce(randomInit(), printStep()))
        ONCE_HOT_COLD -> printSummary(runOnce(hotColdInit(), printStep()))
        MANY_BELL_CURVE -> runTrials()
    }
}

private typealias ReportProgress = (Int, List<Int>) -> Unit

private data class RunResult(
    val stepsNeeded: Int,
    val initAverage: Int,
    val equilibrium: Int
)

private fun runOnce(
    init: List<Int>,
    reportProgress: ReportProgress
): RunResult {
    val initAverage = init.average()
    var step: List<Int> = init
    var nSteps = 0

    reportProgress(nSteps, step)
    while (!step.equilibrium()) {
        ++nSteps
        avoidRunningAway(nSteps)
        step = step.nextStep()
        reportProgress(nSteps, step)
    }
    val equilibrium = step[0]

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

private fun avoidRunningAway(nSteps: Int) {
    if (nSteps >= cutoff) {
        println("MORE THAN $cutoff STEPS NEEDED")
        exitProcess(1)
    }
}

private fun List<Int>.equilibrium(): Boolean {
    val first = first()
    return all { it == first }
}

private fun List<Int>.average() = middle(*(this.toIntArray()))

private fun List<Int>.nextStep(): List<Int> {
    val updated = ArrayList<Int>(size)
    updated.add(middle(first(), second()))
    (1..size - 2).forEach {
        updated.add(middle(preceding(it), current(it), following(it)))
    }
    updated.add(middle(penultimate(), last()))
    return updated
}

private fun List<Int>.second() = this[1]
private fun List<Int>.penultimate() = this[size - 2]
private fun List<Int>.preceding(i: Int) = this[i - 1]
private fun List<Int>.current(i: Int) = this[i]
private fun List<Int>.following(i: Int) = this[i + 1]

private fun middle(vararg xs: Int): Int {
    infix fun Int.outOf(base: Int) =
        if (Random.nextInt(0, base) < this) 1 else 0

    val sum = xs.sum()
    return sum / xs.size + (sum % xs.size outOf xs.size)
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
