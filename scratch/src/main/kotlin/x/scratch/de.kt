package x.scratch

import kotlin.math.absoluteValue
import kotlin.random.Random

private const val n = 40
private const val max = 9

fun main() {
    var last: List<Int> = init()
    println(last)
    var i = 0
    while (true) {
        val next = last.next()
        if (next == last) break
        println(next)
        last = next
        ++i
    }
    println("$i STEPS")
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

private fun List<Int>.next(): List<Int> {
    val updated = ArrayList<Int>(size)
    updated.add(this[0].next(this[1]))
    (1..(size - 2)).forEach {
        updated.add(this[it].next(this[it - 1], this[it + 1]))
    }
    updated.add(this[size - 1].next(this[size - 2]))
    return updated
}

private fun Int.next(other: Int) = (this + other) / 2
private fun Int.next(left: Int, right: Int) = (left + this + right) / 3
