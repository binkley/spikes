package x.scratch

fun main() {
    println()
    println("== COLLATZ")
    for (i in 1..99)
        println(path(i, ArrayList(20)))
}

private tailrec fun path(n: Int, ns: MutableList<Int>): List<Int> {
    ns += n
    if (1 == n) return ns
    return path(next(n), ns)
}

private fun next(n: Int) = when {
    0 == n % 2 -> n / 2
    else -> 3 * n + 1
}
