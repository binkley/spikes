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
        fun createLayer(description: String, script: String) {
            val trimmedScript = script.trimIndent()
            val layer = layers.commit()
            val slot = layer.slot
            println("#$slot - $trimmedScript")
            layer.edit {
                eval("""
                    import hm.binkley.layers.*
                    import hm.binkley.layers.rules.*

                    $trimmedScript
                """, createBindings().apply {
                    this["layer"] = this@edit
                })
            }
            println(layer)

            val file = File("scripts/$slot.kts")
            file.writeText(trimmedScript)
            git.add().addFilepattern("$slot.kts").call()
            val commit = git.commit()
            commit.message = description.trimIndent().trim()
            commit.call()
        }

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
    }

    println(layers)
    println(layers.asMap())

    git.close()
}
