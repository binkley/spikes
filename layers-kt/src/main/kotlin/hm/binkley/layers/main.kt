package hm.binkley.layers

fun main() {
    Baker("/Users/boxley/tmp/layers.git").use {
        it.createLayer("Base rule for 'b'", """
                layer["b"] = last(default=true)
            """, """
                Toggle for "a"
            """.trimIndent())
        it.createLayer("Toggle 'b' off", """
                layer["b"] = false
            """)
        it.createLayer("Base rule for 'a' (complex)", """
                layer["a"] = rule("I am a sum", 0) { context ->
                    if (context["b"]) context.myValues.sum() else -1
                }
            """, """
                Toggle "a" on/off using "b"
            """.trimIndent())
        it.createLayer("Add 2 to 'a'", """
                layer["a"] = 2
            """)
        it.createLayer("Add 3 to 'a'", """
                layer["a"] = 3
            """)
        it.createLayer("Base rule for 'c' (simple)", """
                layer["c"] = sum(default=0)
            """)
        it.createLayer("Add 2 to 'c'", """
                layer["c"] = 2
            """)
        it.createLayer("Add 3 to 'c'", """
                layer["c"] = 3
            """)

        println(it.layers.asMap())
        println(it)
    }
}
