package x.scratch.layers

import x.scratch.layers.BooleanValue.Companion.lastRule
import x.scratch.layers.IntValue.Companion.sumRule
import x.scratch.layers.Layers.Companion.newLayer
import x.scratch.layers.StringValue.Companion.lastRule

fun main() {
    println("INITIAL")

    val layers = newLayer(
        "BOB",
        "edit me",
        "name" to lastRule("The Magnificent Bob"),
        "body" to sumRule(10),
        "mind" to sumRule(10),
        "invisible" to lastRule(false)
    )
    dump(layers)

    println("---")
    println("EDIT WITHOUT KEEPING")

    layers.edit {
        this["name"] = "Sleepy Bob"
    }
    dump(layers)

    println("---")
    println("RESETTING")

    layers.reset()
    dump(layers)

    println("---")
    println("EDIT AND KEEP")

    layers.reset("also edit me")
    layers.edit {
        this["name"] = "The Cool-hand Bob"
        this["invisible"] = true
    }
    layers.keepAndNext("more to do")
    dump(layers)
}

fun dump(layers: Layers) {
    println(layers)
    println(layers.top)
    println(
        """
        My name: ${layers["name"]}
        I am invisible: ${layers["invisible"]}
    """.trimIndent()
    )
}
