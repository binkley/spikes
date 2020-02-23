package x.scratch.layers

interface Rule<T> : (Sequence<T>) -> T {
    val name: String
}

data class LastRule<T>(override val name: String = "last") : Rule<T> {
    override fun invoke(values: Sequence<T>) = values.last()
}

data class SumRule(override val name: String = "sum") : Rule<Int> {
    override fun invoke(values: Sequence<Int>) =
        values.fold(0) { acc, value ->
            acc + value
        }
}
