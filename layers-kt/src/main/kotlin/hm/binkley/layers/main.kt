package hm.binkley.layers

fun main() {
    LayerCake().apply {
        createLayer("Base rule for 'b'", """
                layer["b"] = last(default=true)
            """)
        createLayer("Toggle 'b' off", """
                layer["b"] = false
            """)
        createLayer("Base rule for 'a' (complex)", """
                layer["a"] = rule("I am a sum", 0) { context ->
                    if (context["b"]) context.myValues.sum() else -1
                }
            """)
        createLayer("Add 2 to 'a'", """
                layer["a"] = 2
            """)
        createLayer("Add 3 to 'a'", """
                layer["a"] = 3
            """)
        createLayer("Base rule for 'c' (simple)", """
                layer["c"] = sum(default=0)
            """)
        createLayer("Add 2 to 'c'", """
                layer["c"] = 2
            """)
        createLayer("Add 3 to 'c'", """
                layer["c"] = 3
            """)

        println(layers.asMap())
        println(this)

        close()
    }
}
