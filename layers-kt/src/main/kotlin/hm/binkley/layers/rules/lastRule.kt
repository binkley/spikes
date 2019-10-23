package hm.binkley.layers.rules

import hm.binkley.layers.rule

fun <T> last(default: T) = rule("*Last[=$default]", default) {
    it.myValues.last()
}
