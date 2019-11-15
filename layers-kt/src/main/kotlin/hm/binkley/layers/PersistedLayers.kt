package hm.binkley.layers

import org.eclipse.jgit.api.Git
import java.util.AbstractMap.SimpleEntry
import java.util.Objects.hash
import javax.script.ScriptEngine
import kotlin.collections.Map.Entry

class PersistedLayers(
    private val persistence: Persistence,
    private val scripting: Scripting,
    private val layerList: MutableList<Layer> = mutableListOf()
) : Layers,
    LayersForRuleContext,
    AutoCloseable by persistence {
    init {
        refresh()
    }

    override fun asList(): List<Map<String, Any>> = layerList

    override fun asMap(): Map<String, Any> =
        object : AbstractMap<String, Any>() {
            override val entries: Set<Entry<String, Any>>
                get() = applied().toSortedSet(compareBy {
                    it.key
                })
        }

    @Suppress("UNCHECKED_CAST")
    override fun <T> appliedValueFor(key: String) = topDownLayers.flatMap {
        it.entries
    }.filter {
        it.key == key
    }.first {
        null != it.value.rule
    }.let {
        (it.value.rule!! as Rule<T>)(RuleContext(key, this))
    }

    /** All values for [key] from newest to oldest. */
    @Suppress("UNCHECKED_CAST")
    override fun <T> allValuesFor(key: String) = topDownLayers.mapNotNull {
        it[key]
    }.mapNotNull {
        it.value
    } as List<T>

    @Suppress("UNCHECKED_CAST")
    private fun applied() = topDownLayers.flatMap {
        it.entries
    }.filter {
        null != it.value.rule
    }.map {
        val key = it.key
        val rule = it.value.rule as Rule<Any>
        val value = rule(RuleContext(key, this))
        SimpleEntry(key, value)
    }

    private val topDownLayers: List<Layer>
        get() = layerList.filter {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersistedLayers

        return persistence == other.persistence
                && scripting == other.scripting
                && layerList == other.layerList
    }

    override fun hashCode() = hash(persistence, scripting, layerList)

    override fun toString() =
        "${this::class.simpleName}{persistence=$persistence, scripting=$scripting, layersList=$layerList}"

    internal fun scriptFile(fileName: String) =
        persistence.scriptFile(fileName)

    internal fun <R> letGit(block: (Git) -> R): R =
        persistence.letGit(block)

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
        PersistedLayer(this, layerList.size).also {
            layerList.add(it)
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
