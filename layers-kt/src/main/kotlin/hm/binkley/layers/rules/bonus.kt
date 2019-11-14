package hm.binkley.layers.rules

import hm.binkley.layers.Rule
import hm.binkley.layers.RuleValue
import java.util.Objects

/** Bonus of [otherKey] plus any bonuses from this key. */
class BonusRuleValue(val otherKey: String, default: Int) :
    RuleValue<Int>("*bonus", default, bonusRule(otherKey)) {
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

fun bonusRule(otherKey: String): Rule<Int> = {
    val otherValue: Int = it[otherKey]
    otherValue.toBonus() + it.myValues.sum()
}
