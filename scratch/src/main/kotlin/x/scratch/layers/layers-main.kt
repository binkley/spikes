package x.scratch.layers

import x.scratch.layers.Layers.Companion.newLayer

fun main() {
    val layers = newLayer(
        "BOB",
        "edit me",
        "name" to StringValue.lastRule("The Magnificent Bob"),
        "body" to IntValue.sumRule(10),
        "mind" to IntValue.sumRule(10),
        "invisible" to BooleanValue.lastRule(false)
    )

    println(layers)
    println(layers["name"])

    println("---")

    layers.edit {
        this["name"] = StringValue("Sleepy Bob")
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
        this["name"] = StringValue("The Cool-hand Bob")
    }
    layers.keepAndNext("More to do")
    println(layers)
    println(layers["name"])
}
