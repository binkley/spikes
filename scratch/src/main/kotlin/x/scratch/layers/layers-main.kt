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

    println(layers)
    println(layers["name"])

    println("---")

    layers.edit {
        this["name"] = "Sleepy Bob"
    }

    println(layers)
    println(layers["name"])

    println("---")

    layers.reset()
    println(layers)
    println(layers["name"])

    println("---")

    layers.top.reset("also edit me")
    layers.edit {
        this["name"] = "The Cool-hand Bob"
    }
    layers.keepAndNext("More to do")
    println(layers)
    println(layers["name"])
}
