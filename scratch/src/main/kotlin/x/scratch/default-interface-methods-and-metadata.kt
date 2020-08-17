package x.scratch

import kotlin.reflect.jvm.jvmName

/**
 * See https://blog.jetbrains.com/kotlin/2020/07/kotlin-1-4-m3-generating-default-methods-in-interfaces/
 * See https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-metadata/
 */
fun main() {
    println("==DEFAULT INTERFACE METHODS")

    Nancy().sallyForth()

    Nancy::class.constructors.forEach {
        println(it)
    }

    val metadata = Class.forName(Nancy::class.jvmName)
        .getAnnotation(Metadata::class.java)
    println(metadata.pretty)
}

private val Metadata.pretty
    get() = """metadata {
    bytecodeVersion=${bytecodeVersion.contentToString()}
    data1=${data1.contentToString()}
    data2=${data2.contentToString()}
    extraInt=($extraInt) ${extraInt.extraInt}
    extraString=$extraString
    kind=($kind) ${kind.kind}
    metadataVersion=${metadataVersion.contentToString()}
    packageVersion=$packageName
}"""

private val Int.extraInt
    get() = when (this) {
        0 -> "This is a multi-file class facade or part, compiled with -Xmultifile-parts-inherit"
        1 -> "This class file is compiled by a pre-release version of Kotlin and is not visible to release versions"
        2 -> "This class file is a compiled Kotlin script source file (.kts)"
        3 -> "The metadata of this class file is not supposed to be read by the compiler, whose major.minor version is less than the major.minor version of this metadata (mv)"
        4 -> "This class file is compiled with the new Kotlin compiler backend introduced in Kotlin 1.4"
        5 -> "If the class file is compiled with the new Kotlin compiler backend, the metadata has been verified by the author and no metadata incompatibility diagnostic should be reported at the call site"
        else -> error("Unknown extraInt: $this")
    }

private val Int.kind
    get() = when (this) {
        1 -> "Class"
        2 -> "File"
        3 -> "Synthetic class"
        4 -> "Multi-file class facade"
        5 -> "Multi-file class part"
        else -> error("Unknown KotlinClassHeader.Kind")
    }

private interface BobHead {
    fun sallyForth() = println("Tallyho!")
}

private class Nancy : BobHead
