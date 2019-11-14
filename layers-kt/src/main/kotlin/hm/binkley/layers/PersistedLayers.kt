package hm.binkley.layers

import org.eclipse.jgit.api.Git
import java.util.Objects.hash

class PersistedLayers(
        private val persistence: Persistence,
        private val scripting: Scripting)
    : Layers,
        AutoCloseable by persistence {
    private val layers = PersistedMutableLayers(this)

    init {
        refresh()
    }

    override fun asList() = layers.asList()

    override fun asMap() = layers.asMap()

    override fun createLayer(
            description: String, script: String, notes: String?): Layer {
        val cleanDescription = description.clean()
        val cleanScript = script.clean()
        val layer = newLayer(cleanScript)

        val scriptFile = layer.save(
                cleanDescription, cleanScript, notes?.trimIndent())

        layer.edit {
            metaFromGitFor(scriptFile)
        }

        return layer
    }

    override fun refresh() = persistence.refresh(asList().size) {
        newLayer(it)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersistedLayers

        return persistence == other.persistence
                && scripting == other.scripting
                && layers == other.layers
    }

    override fun hashCode() = hash(persistence, scripting, layers)

    override fun toString() =
            "${this::class.simpleName}{persistence=$persistence, scripting=$scripting, layers=$layers}"

    internal fun scriptFile(fileName: String) =
            persistence.scriptFile(fileName)

    internal fun <R> withGit(block: Git.() -> R): R =
            persistence.withGit(block)

    private fun newLayer(script: String): Layer {
        return scripting.withEngine {
            val layer = layers.commit(script)

            layer.edit {
                eval("""
                    import hm.binkley.layers.*
                    import hm.binkley.layers.rules.*

                    $script
                """, createBindings().apply {
                    this["layer"] = this@edit
                })
            }

            layer
        }
    }

    private fun MutableLayer.metaFromGitFor(scriptFile: String) = apply {
        persistence.withGit {
            log().addPath(scriptFile).call().first().also {
                meta["commit-time"] = it.commitTime.toIsoDateTime()
                meta["full-message"] = it.fullMessage
            }
        }
    }
}
