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
    val init = (1..(n - 2)).map {
        Random.nextInt() % (max + 1)
    }.map {
        it.absoluteValue
    }.toMutableList()
    init.add(0, 0)
    init.add(n - 1, 0)
    return init
}

private fun List<Int>.next(): List<Int> {
    val updated = ArrayList<Int>(size)
    updated.add(this[0])
    (1..(size - 2)).forEach {
        updated.add(this[it].next(this[it - 1], this[it + 1]))
    }
    updated.add(last())
    return updated
}

private fun Int.next(left: Int, right: Int) = when {
    this < left && this < right -> randomPick(left, right)
    this > left && this > right -> randomPick(left, right)
    else -> this
}

private fun randomPick(left: Int, right: Int) = when {
    Random.nextBoolean() -> left
    else -> right
}
