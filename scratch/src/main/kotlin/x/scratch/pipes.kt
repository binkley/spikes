package x.scratch

fun main() {
    listOf(1, 2, 3) / { 2 * it } / { println(it) }
}

operator fun <T, U> Iterable<T>.div(pipe: (T) -> U) = map(pipe)
