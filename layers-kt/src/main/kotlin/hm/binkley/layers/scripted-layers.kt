package hm.binkley.layers

import javax.script.ScriptEngine

interface ScriptedLayer {
    val included: List<String>
    fun <R> letEngine(block: (ScriptEngine) -> R): R
    fun include(script: String): Boolean
}

interface ScriptedLayerMutation {
    fun execute(script: String)
}

interface ScriptedLayers {
    fun <R> letEngine(block: (ScriptEngine) -> R): R
}
