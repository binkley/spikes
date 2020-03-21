package x.scratch

import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.system.exitProcess

private const val noisy = false
private const val n = 20
private const val max = 9
private const val cutoff = 1000
private const val trials = 100

fun main() {
    val counts = mutableListOf(0, 0, 0)
    (1..trials).forEach {
        val (init, final) = oneTrial()
        when {
            init < final -> ++counts[0]
            init == final -> ++counts[1]
            init > final -> ++counts[2]
        }
    }
    println(counts)
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
    init.add(0)
    (1 until (n - 1)).map {
        Random.nextInt() % (max + 1)
    }.map {
        it.absoluteValue
    }.forEach {
        init.add(it)
    }
    init.add(0)
    return init
}

private fun List<Int>.equilibrium(): Boolean {
    val first = this[0]
    (1 until size).forEach {
        if (this[it] != first) return false
    }
    return true
}

private fun List<Int>.average() = sum() / size

private fun List<Int>.next(): List<Int> {
    val updated = ArrayList<Int>(size)
    updated.add(this[0].next(this[1]))
    (1 until (size - 1)).forEach {
        updated.add(this[it].next(this[it - 1], this[it + 1]))
    }
    updated.add(this[size - 1].next(this[size - 2]))
    return updated
}

private fun Int.next(other: Int) = middle(this, other)
private fun Int.next(left: Int, right: Int) = middle(left, this, right)

private fun middle(vararg xs: Int): Int {
    infix fun Int.outOf(base: Int) =
        if (Random.nextInt(0, base) < this) 1 else 0

    val sum = xs.sum()
    return sum / xs.size + ((sum % xs.size) outOf xs.size)
}
