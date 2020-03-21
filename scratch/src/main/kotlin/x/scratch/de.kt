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

private fun middle(a: Int, b: Int, c: Int): Int {
    val sum = a + b + c
    return when (sum % 3) {
        0 -> sum / 3
        1 -> sum / 3 + oneInNChance(3)
        else -> sum / 3 + twoThirdsChance()
    }
}

private fun middle(a: Int, b: Int): Int {
    val sum = a + b
    return when (sum % 2) {
        0 -> sum / 2
        else -> sum / 2 + oneInNChance(2)
    }
}

private fun oneInNChance(n: Int): Int {
    return when (Random.nextInt(0, n)) {
        0 -> 1
        else -> 0
    }
}

private fun twoThirdsChance(): Int {
    return when (Random.nextInt(0, 3)) {
        0, 1 -> 1
        else -> 0
    }
}
