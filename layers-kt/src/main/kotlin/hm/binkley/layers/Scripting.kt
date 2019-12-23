package hm.binkley.layers

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class Scripting {
    private val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    internal fun <R> letEngine(block: (ScriptEngine) -> R) =
        engine.let(block)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Scripting

        return true
    }

    override fun hashCode() = this::class.hashCode()

    override fun toString() = "${this::class.simpleName}{}"
}
