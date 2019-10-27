package hm.binkley.layers.rules

import hm.binkley.layers.Rule
import hm.binkley.layers.rule

fun total(default: Int) = rule("*total", default, totalRule())

fun totalRule(): Rule<Int> = {
    it.myValues.sum()
}
