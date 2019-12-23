package hm.binkley.layers

import java.util.Objects.hash

typealias Rule<T> = (RuleContext<T>) -> T

open class Value<T>(val rule: Rule<T>?, val value: T?) : Diffable {
    protected fun printSimple(value: Any?) = when (value) {
        is String -> "\"${value}\""
        else -> value.toString()
    }

    // TODO: PersistedValue
    open fun toSourceCode() = printSimple(value)

    // TODO: Print "3 : Int" rather than just "3"
    //  Expression in a class literal has a nullable type 'T', use !! to make
    //  the type non-nullable
    override fun toDiff() = if (null == rule) "$value" else "$this"

    override fun equals(other: Any?) = this === other
            || other is Value<*>
            && rule == other.rule
            && value == other.value

    override fun hashCode() = hash(rule, value)

    override fun toString() =
        "${this::class.simpleName}{rule=$rule, value=$value}"
}

open class RuleValue<T>(val name: String, val default: T, rule: Rule<T>) :
    Value<T>(rule, default) {
    override fun toSourceCode(): String {
        return rule.toString()
    }

    override fun equals(other: Any?) = this === other
            || other is RuleValue<*>
            && name == other.name
            && default == other.default
            && rule == other.rule

    override fun hashCode() = hash(name, default, rule)

    override fun toString() =
        "${this::class.simpleName}<rule: $name[=$default]>"
}

fun <T> value(context: T): Value<T> =
    Value(null, context)

fun <T> rule(name: String, default: T, rule: Rule<T>) =
    RuleValue(name, default, rule)

class RuleContext<T>(
    val myKey: String,
    private val layers: LayersForRuleContext
) {
    val myValues: List<T>
        get() = layers.allValuesFor(myKey)

    operator fun <T> get(key: String) = layers.appliedValueFor<T>(key)
}

interface LayersForRuleContext {
    fun <T> appliedValueFor(key: String): T

    fun <T> allValuesFor(key: String): List<T>
}
