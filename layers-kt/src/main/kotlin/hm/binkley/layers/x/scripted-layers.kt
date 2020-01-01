package hm.binkley.layers.x

import javax.script.ScriptEngine

interface ScriptedForLayer {
    val included: List<String>
    fun <R> letEngine(block: (ScriptEngine) -> R): R
    fun include(script: String): Boolean
}

interface ScriptedForLayerMutation {
    fun execute(script: String)
}

interface ScriptedForLayers {
    fun <R> letEngine(block: (ScriptEngine) -> R): R
}
