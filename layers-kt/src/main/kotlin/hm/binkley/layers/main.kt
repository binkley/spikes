package hm.binkley.layers

import org.eclipse.jgit.api.Git
import java.io.File
import javax.script.ScriptEngineManager

fun main() {
    val scriptsDir = File("./scripts")
    val git = if (scriptsDir.exists())
        Git.open(scriptsDir)
    else {
        val git = Git.init().setDirectory(scriptsDir).call()
        val commit = git.commit()
        commit.message = "Init"
        commit.call()
        git
    }

    val layers = Layers()
    val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    with(engine) {
        fun createLayer(script: String) {
            val trimmedScript = script.trimIndent()
            val (layer, index) = layers.commit()
            println("#$index - $trimmedScript")
            layer.edit {
                eval("""
                    import hm.binkley.layers.*
                    import hm.binkley.layers.rules.*

                    $trimmedScript
                """.trimIndent(), createBindings().apply {
                    this["layer"] = this@edit
                })
            }
            println(layer)

            val file = File("scripts/$index.kts")
            file.writeText(trimmedScript)
            git.add().addFilepattern("$index.kts").call()
            val commit = git.commit()
            commit.message = "???"
            commit.call()
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

    git.close()
}
