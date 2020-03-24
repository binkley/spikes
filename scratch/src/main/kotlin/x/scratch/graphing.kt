package x.scratch

import kotlin.math.max
import kotlin.math.min

/**
 * Prints a vertical ASCII bar graph of differences among values in [row].
 */
fun graphDifferences(row: List<Int>) {
    for (value in row.max() downTo row.min()) {
        if (graphRowOrStop(value, row))
            break
    }
    println("-".repeat(row.size))
}

private fun graphRowOrStop(value: Int, row: List<Int>): Boolean {
    row.forEach {
        print(if (it < value) ' ' else '|')
    }
    println()
    return row.all { it >= value }
}

private fun List<Int>.max(): Int {
    val iter = iterator()
    var max = iter.next()
    while (iter.hasNext()) max = max(max, iter.next())
    return max
}

private fun List<Int>.min(): Int {
    val iter = iterator()
    var min = iter.next()
    while (iter.hasNext()) min = min(min, iter.next())
    return min
}
