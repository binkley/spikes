package hm.binkley.layers

fun PersistedLayers.noisyCreateLayer(description: String, script: String,
        notes: String? = null) {
    val layer = createLayer(description, script, notes)
    println("#${layer.slot}:\n${layer.script}")
    println(layer.forDiff())
}

fun main(args: Array<String>) {
    val repository = if (false) args[0]
    else "/Users/boxley/tmp/layers.git"
    PersistedLayers(repository).use {
        it.noisyCreateLayer("Base rule for 'b'", """
                layer["b"] = last(default=true)
            """, """
                Toggle for "a"
            """)
        it.noisyCreateLayer("Toggle 'b' off", """
                layer["b"] = false
            """)
        it.noisyCreateLayer("Base rule for 'a' (complex)", """
                layer["a"] = rule("I am a sum", 0) { context ->
                    if (context["b"]) context.myValues.sum() else -1
                }
            """, """
                Toggle "a" on/off using "b"
            """)
        it.noisyCreateLayer("Base rule for 'c' (simple)", """
                layer["c"] = sum(default=0)
            """)
        it.noisyCreateLayer("Add 2 to both 'a' and 'c'", """
                layer["a"] = 2
                layer["c"] = 2
            """)
        it.noisyCreateLayer("Add 3 to 'a'", """
                layer["a"] = 3
            """)
        it.noisyCreateLayer("Do nothing", """
            """)
        it.noisyCreateLayer("Add 3 to 'c'", """
                layer["c"] = 3
            """)

        println(it.asList())
        println(it.asMap())
        println(it)
    }
}
