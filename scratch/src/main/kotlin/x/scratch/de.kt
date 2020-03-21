package x.scratch

import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.system.exitProcess

private const val noisy = false
private const val n = 20
private const val max = 9
private const val cutoff = 10000
private const val oneTimeOnly = false
private const val trials = 100

fun main() {
    if (oneTimeOnly) {
        oneTrial()
    } else {
        val counts = mutableListOf(0, 0, 0)
        repeat(trials) {
            val (init, final) = oneTrial()
            when {
                init < final -> ++counts[0]
                init == final -> ++counts[1]
                init > final -> ++counts[2]
            }
        }
        println(counts)
    }
}

private fun oneTrial(): Pair<Int, Int> {
    // TODO: BUG: Something biases this towards INIT < FINAL
    var last: List<Int> = init()
    val initAverage = last.average()
    var i = 0

    noise("$i: $last")
    while (!last.equilibrium()) {
        ++i
        avoidRunningAway(i)
        last = last.next()
        noise("$i: $last")
    }
    val equilibrium = last[0]

    noise("$i STEPS NEEDED")
    noise("$initAverage INITIAL AVERAGE")
    noise("$equilibrium EQUILIBRIUM")

    return initAverage to equilibrium
}

private fun noise(message: String) {
    if (noisy) println(message)
}

private fun avoidRunningAway(i: Int) {
    if (i >= cutoff) {
        println("MORE THAN $cutoff STEPS NEEDED")
        exitProcess(1)
    }
}

private fun init(): MutableList<Int> {
    val init = ArrayList<Int>(n)
    repeat(n) {
        init.add(Random.nextInt(0, max + 1).absoluteValue)
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

private fun List<Int>.average() = sum() / size

private fun List<Int>.next(): List<Int> {
    val updated = ArrayList<Int>(size)
    updated.add(first().next(this[1]))
    repeat(size - 2) {
        val i = it + 1
        updated.add(this[i].next(this[i - 1], this[i + 1]))
    }
    updated.add(last().next(this[size - 2]))
    return updated
}

private fun Int.next(other: Int) = middle(this, other)
private fun Int.next(left: Int, right: Int) = middle(left, this, right)

private fun middle(vararg xs: Int): Int {
    infix fun Int.outOf(base: Int) =
        if (Random.nextInt(0, base) < this) 1 else 0

    val sum = xs.sum()
    return sum / xs.size + (sum % xs.size outOf xs.size)
}
