package x.scratch

typealias PIPE<T, U> = (T) -> U

fun main() {
    val double = { it: Int -> 2 * it }

    listOf(1, 2, 3) / double / { println(it) }
    // TODO: Does not compile: "{ 2 * it }" inferred to return Unit
    listOf(1, 2, 3) / { it: Int -> 2 * it } as PIPE / { println(it) }
}

operator fun <T, U> Iterable<T>.div(pipe: (T) -> U): Iterable<U> = map(pipe)
operator fun <T> Iterable<T>.div(pipe: (T) -> Unit): Unit = forEach(pipe)
