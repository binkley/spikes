package hm.binkley.layers.rules

import hm.binkley.layers.rule

fun sum(default: Int) = rule("*Sum[=$default]", default) {
    it.myValues.sum()
}
