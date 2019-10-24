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
        load()
    }

    private fun load() {
        scriptsDir.list { dir, name ->
            name.endsWith(".kts")
        }!!.sortedBy {
            it.removeSuffix(".kts").toInt()
        }.map {
            scriptsDir.resolve(it).readText()
        }.forEach {
            layers.new(it)
        }
    }

    fun close() = git.close()

    fun createLayer(description: String, script: String) {
        val trimmedScript = script.trimIndent()

        val layer = layers.new(trimmedScript)
        println("#${layer.slot} - $trimmedScript")
        println(layer.forDiff())

        layer.save(description, trimmedScript)
    }

    override fun toString() =
            "${this::class.simpleName}{scriptsDir=$scriptsDirPath, layers=$layers}"

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
            val scriptFile = File("$scriptsDirPath/$slot.kts")
            scriptFile.writeText(trimmedScript)
            scriptFile.appendText("\n")
            val diffFile = File("$scriptsDirPath/$slot.txt")
            diffFile.writeText(forDiff())
            diffFile.appendText("\n")

            add().addFilepattern("$slot.kts").call()
            add().addFilepattern("$slot.txt").call()

            val commit = commit()
            commit.message = description.trimIndent().trim()
            commit.call()
        }
    }
}
