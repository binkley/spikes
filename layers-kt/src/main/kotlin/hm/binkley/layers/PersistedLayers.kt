package hm.binkley.layers

import org.eclipse.jgit.api.Git
import java.util.Objects.hash
import javax.script.ScriptEngine

class PersistedLayers(
    private val persistence: GitPersistence,
    private val scripting: Scripting,
    private val _layers: MutableList<PersistedLayer> = mutableListOf()
) : Layers,
    LayersForRuleContext,
    AutoCloseable by persistence {
    init {
        refresh()
    }

    override val layers: List<Layer> = _layers

    override fun asList(): List<Map<String, Any>> = _layers

    // TODO: Simplify
    override fun asMap(): Map<String, Any> =
        topDownLayers.flatMap {
            it.entries
        }.filter {
            null != it.value.rule
        }.map {
            val key = it.key
            @Suppress("UNCHECKED_CAST")
            val rule = it.value.rule as Rule<Any>
            val value = rule(RuleContext(key, this))
            key to value
        }.asReversed().toMap().toSortedMap()

    override fun <T> appliedValueFor(key: String) = topDownLayers.flatMap {
        it.entries
    }.filter {
        it.key == key
    }.first {
        null != it.value.rule
    }.let {
        @Suppress("UNCHECKED_CAST")
        (it.value.rule!! as Rule<T>)(RuleContext(key, this))
    }

    /** All values for [key] from newest to oldest. */
    @Suppress("UNCHECKED_CAST")
    override fun <T> allValuesFor(key: String) = topDownLayers.mapNotNull {
        it[key]
    }.mapNotNull {
        it.value
    } as List<T>

    private val topDownLayers: List<Layer>
        get() = _layers.filter {
            it.enabled
        }.asReversed()

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

    override fun newLayer(): Layer {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun equals(other: Any?) = this === other
            || other is PersistedLayers
            && persistence == other.persistence
            && scripting == other.scripting
            && _layers == other._layers

    override fun hashCode() = hash(persistence, scripting, _layers)

    override fun toString() =
        "${this::class.simpleName}{persistence=$persistence, scripting=$scripting, layersList=$_layers}"

    internal fun toSourceCode() = _layers.map {
        it.toSourceCode()
    }

    internal fun scriptFile(fileName: String) =
        persistence.scriptFile(fileName)

    internal fun <R> letGit(block: (Git) -> R): R = persistence.letGit(block)

    internal fun <R> letEngine(block: (ScriptEngine) -> R): R =
        scripting.letEngine(block)

    private fun createLayer(script: String): PersistedLayer =
        scripting.letEngine { engine ->
            commit(script).also { layer ->
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

    internal fun commit(script: String): PersistedLayer =
        PersistedLayer(this, _layers.size).also {
            _layers.add(it)
            it.include(script)
        }

    private fun refresh() = persistence.refresh(asList().size) {
        createLayer(it)
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
