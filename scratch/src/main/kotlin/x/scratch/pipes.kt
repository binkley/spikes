package x.scratch

fun main() {
    println("== PIPES")
    val input = listOf(1, 2, 3)
    println("DOUBLING $input ->")
    input / { 2 * it } / { println(it) }
}

operator fun <T, U> Iterable<T>.div(pipe: (T) -> U) = map(pipe)
