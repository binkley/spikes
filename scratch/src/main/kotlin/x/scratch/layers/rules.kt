package x.scratch.layers

interface Rule<T> : (Sequence<T>) -> T

data class LastRule<T>(val name: String = "last") : Rule<T> {
    override fun invoke(values: Sequence<T>) = values.last()
}

data class SumRule(val name: String = "sum") : Rule<Int> {
    override fun invoke(values: Sequence<Int>) =
        values.fold(0) { acc, value ->
            acc + value
        }
}
