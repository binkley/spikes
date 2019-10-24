package hm.binkley.layers.rules

import hm.binkley.layers.rule

fun <T> last(default: T) = rule("*last", default) {
    it.myValues.last()
}
