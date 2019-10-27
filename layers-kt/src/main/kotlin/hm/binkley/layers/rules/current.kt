package hm.binkley.layers.rules

import hm.binkley.layers.Rule
import hm.binkley.layers.rule

fun <T> current(default: T) = rule("*current", default, currentRule())

fun <T> currentRule(): Rule<T> = {
    it.myValues.last()
}
