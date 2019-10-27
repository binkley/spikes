package hm.binkley.layers.rules

import hm.binkley.layers.RuleContext
import hm.binkley.layers.RuleValue
import java.util.Objects

/** Bonus of [otherKey] plus any bonuses from this key. */
class BonusRuleValue(val otherKey: String, default: Int)
    : RuleValue<Int>("*name", default, compute(otherKey)) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BonusRuleValue

        return default == other.default
                && otherKey == other.otherKey
    }

    override fun hashCode() =
            Objects.hash(default, otherKey)

    override fun toString() =
            "${this::class.simpleName}<rule: *bonus[=$otherKey/$default]>"
}

fun Int.toBonus() = this / 2 - 5

fun bonus(otherKey: String, default: Int = 0) =
        BonusRuleValue(otherKey, default)

private fun compute(otherKey: String) = { it: RuleContext<Int> ->
    val otherValue: Int = it[otherKey]
    otherValue.toBonus() + it.myValues.sum()
}
