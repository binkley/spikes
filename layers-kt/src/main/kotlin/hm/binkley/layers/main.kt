package hm.binkley.layers

fun Baker.myCreateLayer(description: String, script: String,
        notes: String? = null) {
    val layer = createLayer(description, script, notes)
    println("#${layer.slot} - ${layer.script}")
    println(layer.forDiff())
}

fun main(args: Array<String>) {
    Baker(args[0]).use {
        it.myCreateLayer("Base rule for 'b'", """
                layer["b"] = last(default=true)
            """, """
                Toggle for "a"
            """)
        it.myCreateLayer("Toggle 'b' off", """
                layer["b"] = false
            """)
        it.myCreateLayer("Base rule for 'a' (complex)", """
                layer["a"] = rule("I am a sum", 0) { context ->
                    if (context["b"]) context.myValues.sum() else -1
                }
            """, """
                Toggle "a" on/off using "b"
            """)
        it.myCreateLayer("Add 2 to 'a'", """
                layer["a"] = 2
            """)
        it.myCreateLayer("Add 3 to 'a'", """
                layer["a"] = 3
            """)
        it.myCreateLayer("Base rule for 'c' (simple)", """
                layer["c"] = sum(default=0)
            """)
        it.myCreateLayer("Add 2 to 'c'", """
                layer["c"] = 2
            """)
        it.myCreateLayer("Add 3 to 'c'", """
                layer["c"] = 3
            """)

        println(it.layers.asMap())
        println(it)
    }
}
