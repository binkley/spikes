package hm.binkley.layers

import org.eclipse.jgit.api.Git
import java.util.Objects
import java.util.TreeMap

class PersistedLayer(
    private val layers: PersistedLayers,
    override val slot: Int,
    override val script: String,
    private val contents: MutableMap<String, Value<*>> = TreeMap()
) : Map<String, Value<*>> by contents,
    Layer {
    private val _meta: MutableMap<String, String> = mutableMapOf()

    override val enabled = true
    override val meta: Map<String, String>
        get() = _meta

    override fun toDiff() = contents.entries.joinToString("\n") {
        val (key, value) = it
        "$key: ${value.toDiff()}"
    }

    override fun edit(block: MutableLayer.() -> Unit): Layer = apply {
        PersistedMutableLayer(_meta, contents).block()
    }

    override fun save(
        cleanDescription: String,
        cleanScript: String,
        cleanNotes: String?
    ): String {
        fun Git.write(ext: String, contents: String) {
            val fileName = "$slot.$ext"
            val scriptFile = layers.scriptFile(fileName)
            scriptFile.writeText(contents)
            if (contents.isNotEmpty())
                scriptFile.appendText("\n")
            add().addFilepattern(fileName).call()
        }

        return layers.letGit { git ->
            git.write("kts", cleanScript)
            val diff = toDiff()
            if (diff.isNotEmpty())
                git.write("txt", diff)
            cleanNotes?.also {
                git.write("notes", it)
            }

            val commit = git.commit()
            commit.message = cleanDescription
            commit.call()

            git.push().call()

            "$slot.kts"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersistedLayer

        return slot == other.slot
                && script == other.script
                && contents == other.contents
                && enabled == other.enabled
    }

    override fun hashCode() =
        Objects.hash(
            slot, script, contents,
            enabled
        )

    override fun toString() =
        "${this::class.simpleName}#$slot:$contents\\$meta[${if (enabled) "enabled" else "disabled"}]"
}
