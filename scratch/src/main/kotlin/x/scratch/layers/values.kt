package x.scratch.layers

sealed class Value<T> {
    abstract val value: T
}

data class BooleanValue(override val value: Boolean) : Value<Boolean>() {
    companion object {
        fun lastRule(initialValue: Boolean) =
            RuleValue(LastRule(), initialValue)
    }
}

data class IntValue(override val value: Int) : Value<Int>() {
    companion object {
        fun sumRule(initialValue: Int) =
            RuleValue(SumRule(), initialValue)
    }
}

data class StringValue(override val value: String) : Value<String>() {
    companion object {
        fun lastRule(initialValue: String) =
            RuleValue(LastRule(), initialValue)
    }
}

data class RuleValue<T>(val rule: Rule<T>, val initialValue: T) :
    Value<T>(), Rule<T> by rule {
    override val value = initialValue
}
