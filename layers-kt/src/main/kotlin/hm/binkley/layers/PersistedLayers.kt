package hm.binkley.layers

import org.eclipse.jgit.api.Git
import java.util.Objects.hash

class PersistedLayers(
    private val persistence: Persistence,
    private val scripting: Scripting
) : Layers,
    AutoCloseable by persistence {
    private val layers = PersistedMutableLayers(this)

    init {
        refresh()
    }

    override fun asList() = layers.asList()

    override fun asMap() = layers.asMap()

    override fun newLayer(
        description: String, script: String, notes: String?
    ): Layer {
        val cleanDescription = description.clean()
        val cleanScript = script.clean()
        val cleanNotes = notes?.clean()

        return createLayer(cleanScript).apply {
            edit {
                metaFromGitFor(
                    save(cleanDescription, cleanScript, cleanNotes)
                )
            }
        }
    }

    override fun refresh() = persistence.refresh(asList().size) {
        createLayer(it)
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

    internal fun <R> letGit(block: (Git) -> R): R =
        persistence.letGit(block)

    private fun createLayer(script: String) = scripting.letEngine { engine ->
        layers.commit(script).also { layer ->
            layer.edit {
                engine.eval("""
                    import hm.binkley.layers.*
                    import hm.binkley.layers.rules.*
    
                    $script
                """, engine.createBindings().also {
                    it["layer"] = this
                })
            }
        }
    }

    private fun MutableLayer.metaFromGitFor(scriptFile: String) = apply {
        persistence.letGit { git ->
            git.log().addPath(scriptFile).call().first().also {
                meta["commit-time"] = it.commitTime.toIsoDateTime()
                meta["full-message"] = it.fullMessage
            }
        }
    }
}
