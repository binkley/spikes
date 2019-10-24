package hm.binkley.layers.rules

import hm.binkley.layers.rule

fun sum(default: Int) = rule("*sum", default) {
    it.myValues.sum()
}
