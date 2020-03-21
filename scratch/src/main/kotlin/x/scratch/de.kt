package x.scratch

import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.system.exitProcess

private const val n = 20
private const val max = 9
private const val cutoff = 1000

fun main() {
    var last: List<Int> = init()
    val initAverage = last.average()
    var i = 0

    println("$i: $last")
    while (!last.equilibrium()) {
        ++i
        avoidRunningAway(i)
        last = last.next()
        println("$i: $last")
    }

    println("$i STEPS NEEDED")
    println("$initAverage INITIAL AVERAGE")
    println("${last[0]} EQUILIBRIUM")
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
    (1..(n - 2)).map {
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
    (1..(size - 2)).forEach {
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
