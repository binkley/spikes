package hm.binkley.layers

import org.eclipse.jgit.api.Git
import java.util.Objects
import java.util.TreeMap

class PersistedLayer(
        private val layers: PersistedLayers,
        override val slot: Int,
        override val script: String,
        override val meta: MutableMap<String, String> = mutableMapOf(),
        private val contents: MutableMap<String, Value<*>> = TreeMap())
    : Map<String, Value<*>> by contents,
        Layer {
    override val enabled = true

    override fun toDiff() = contents.entries.joinToString("\n") {
        val (key, value) = it
        "$key: ${value.toDiff()}"
    }

    override fun edit(block: PersistedMutableLayer.() -> Unit): Layer =
            apply {
                val mutable = PersistedMutableLayer(contents)
                mutable.block()
            }

    override fun save(description: String, trimmedScript: String,
            notes: String?)
            : String {
        fun Git.write(ext: String, contents: String) {
            val fileName = "$slot.$ext"
            val scriptFile = layers.scriptFile(fileName)
            scriptFile.writeText(contents)
            if (contents.isNotEmpty())
                scriptFile.appendText("\n")
            add().addFilepattern(fileName).call()
        }

        return layers.withGit {
            write("kts", trimmedScript)
            val diff = toDiff()
            if (diff.isNotEmpty())
                write("txt", diff)
            notes?.also {
                write("notes", it)
            }

            val commit = commit()
            commit.message = description.trimIndent().trim()
            commit.call()

            push().call()

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
            Objects.hash(slot, script, contents,
                    enabled)

    override fun toString() =
            "${this::class.simpleName}#$slot:$contents\\$meta[${if (enabled) "enabled" else "disabled"}]"
}
