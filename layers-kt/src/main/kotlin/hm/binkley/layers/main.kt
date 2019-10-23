package hm.binkley.layers

import javax.script.ScriptEngineManager

fun main() {
    val layers = Layers()
//    layers.commit().edit {
//        this["Q"] = rule("*Anonymous", 0) { context ->
//            if (context["b"]) context.myValues.sum() else -1
//        }
//    }

    val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    with(engine) {
        fun createLayer(script: String) {
            layers.commit().edit {
                eval("""
                    import hm.binkley.layers.*
                    import hm.binkley.layers.rules.*

                    ${script.trimIndent()}
                """.trimIndent(), createBindings().apply {
                    this["layer"] = this@edit
                })
            }
        }

        createLayer("""
                layer["b"] = last(default=true)
            """)
        createLayer("""
                layer["b"] = false
            """)
        createLayer("""
                layer["a"] = rule("I am a sum", 0) { context ->
                    if (context["b"]) context.myValues.sum() else -1
                }
            """)
        createLayer("""
                layer["a"] = 2
            """)
        createLayer("""
                layer["a"] = 3
            """)
        createLayer("""
                layer["c"] = sum(default=0)
            """)
        createLayer("""
                layer["c"] = 2
            """)
        createLayer("""
                layer["c"] = 3
            """)
    }

    println(layers)
    println(layers.asMap())
}
