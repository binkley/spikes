package hm.binkley.layers

import org.eclipse.jgit.api.Git
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Objects
import javax.script.ScriptEngineManager

class PersistedLayers(private val repository: String)
    : Layers {
    private val scriptsDir = Files.createTempDirectory("layers")
    private val git = Git.cloneRepository()
            .setDirectory(scriptsDir.toFile())
            .setURI(repository)
            .call()
    private val engine = ScriptEngineManager().getEngineByExtension("kts")!!
    private val layers = PersistedMutableLayers(this)

    init {
        refresh()
    }

    override fun asList() = layers.asList()

    override fun asMap() = layers.asMap()

    override fun close() {
        git.close()
        scriptsDir.recursivelyDelete()
    }

    fun refresh() = scriptsDir.load()

    fun createLayer(description: String, script: String,
            notes: String? = null): Layer {
        val cleanDescription = description.clean()
        val cleanScript = script.clean()
        val layer = layers.new(cleanScript)

        val scriptFile = layer.save(
                cleanDescription, cleanScript, notes?.trimIndent())

        return layer.addMetaFor(scriptFile)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersistedLayers

        return repository == other.repository
                && layers == other.layers
    }

    override fun hashCode() = Objects.hash(repository, layers)

    override fun toString() =
            "${this::class.simpleName}{repository=$repository, scriptsDir=$scriptsDir, layers=$layers}"

    internal fun scriptFile(fileName: String): File {
        val scriptsDirFile = scriptsDir.toFile()
        return File("$scriptsDirFile/$fileName")
    }

    internal fun <R> withGit(block: Git.() -> R): R = with(git, block)

    private fun PersistedMutableLayers.new(script: String): Layer {
        with(engine) {
            val layer = commit(script)

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

    private fun Path.load() {
        val scriptsDirFile = toFile()
        val scripts = scriptsDirFile.list { _, name ->
            name.endsWith(".kts")
        }!!.sortedBy {
            it.removeSuffix(".kts").toInt()
        }

        scripts.subList(asList().size, scripts.size).map {
            scriptsDirFile.resolve(it).readText().trim()
        }.forEach {
            layers.new(it)
        }
    }

    private fun Layer.addMetaFor(scriptFile: String) = apply {
        git.log().addPath(scriptFile).call().first().also {
            meta["commit-time"] = it.commitTime.toIsoDateTime()
            meta["full-message"] = it.fullMessage
        }
    }
}
