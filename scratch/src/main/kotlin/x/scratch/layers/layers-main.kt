package x.scratch.layers

import x.scratch.layers.Layers.Companion.newLayer

fun main() {
    val layers = newLayer(
        "edit me",
        "name" to StringValue.lastRule("The Magnificent Bob"),
        "body" to IntValue.sumRule(10),
        "mind" to IntValue.sumRule(10),
        "invisible" to BooleanValue.lastRule(false)
    )

    println(layers)
    println(layers["name"])
}
