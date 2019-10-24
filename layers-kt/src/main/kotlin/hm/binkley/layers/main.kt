package hm.binkley.layers

import org.eclipse.jgit.api.Git
import java.io.File
import javax.script.ScriptEngineManager

class LayerCake(
        val layers: Layers = Layers(),
        val scriptsDirPath: String = "./scripts") {
    private val scriptsDir = File(scriptsDirPath)
    private val git =
            if (scriptsDir.exists()) Git.open(scriptsDir)
            else Git.init().setDirectory(scriptsDir).call()
    private val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    init {

    }

    fun close() = git.close()

    fun createLayer(description: String, script: String) {
        val trimmedScript = script.trimIndent()

        val layer = layers.new(trimmedScript)
        println("#${layer.slot} - $trimmedScript")
        println(layer)

        layer.save(description, trimmedScript)
    }

    override fun toString() =
            "${LayerCake::class.simpleName}{scriptsDir=$scriptsDirPath, layers=$layers}"

    private fun Layers.new(script: String): Layer {
        with(engine) {
            val layer = commit()

            layer.edit {
                eval("""
                    import hm.binkley.layers.*
                    import hm.binkley.layers.rules.*

                    $script
                """, createBindings().apply {
                    this["layer"] = this@edit
                })
            }

            return layer
        }
    }

    private fun Layer.save(description: String, trimmedScript: String) {
        with(git) {
            val file = File("scripts/$slot.kts")
            file.writeText(trimmedScript)
            file.appendText("\n")

            add().addFilepattern("$slot.kts").call()

            val commit = commit()
            commit.message = description.trimIndent().trim()
            commit.call()
        }
    }
}

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
