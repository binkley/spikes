package x.scratch.layers

import x.scratch.layers.BooleanValue.Companion.lastRule
import x.scratch.layers.IntValue.Companion.sumRule
import x.scratch.layers.Layers.Companion.newLayer
import x.scratch.layers.StringValue.Companion.lastRule

fun main() {
    val layers = newLayer(
        "BOB",
        "edit me",
        "name" to lastRule("The Magnificent Bob"),
        "body" to sumRule(10),
        "mind" to sumRule(10),
        "invisible" to lastRule(false)
    )
    dump("INITIAL", layers)

    layers.edit {
        this["name"] = "Sleepy Bob"
    }
    dump("EDIT WITHOUT KEEPING", layers)

    layers.reset()
    dump("RESETTING", layers)

    layers.top.reset("should be unseen")
    layers.reset("also edit me")
    layers.edit {
        this["name"] = "The Cool-hand Bob"
        this["invisible"] = true
    }
    layers.keepAndNext("more to do")
    dump("EDIT AND KEEP", layers)
}

fun dump(what: String, layers: Layers) {
    println("---")
    println(what)
    println(layers)
    println(layers.toMap())
    println(layers.top)
    println(
        """
        My name: ${layers["name"]}
        I am invisible: ${layers["invisible"]}
    """.trimIndent()
    )
}
