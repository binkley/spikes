package x.scratch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule

/** See https://www.mkammerer.de/blog/kotlin-and-yaml-part-2/ */
fun main() {
    println("== YAML")

    val yaml = """
        q: 3
        """.trimIndent()
    val q = load<Qux>(yaml)
    println("Q -> $q")
}

private data class Qux(val q: Int)

private inline fun <reified T> load(yaml: String) = yaml.reader().use {
    ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule())
    }.readValue(it, T::class.java)
}
