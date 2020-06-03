package x.scratch

import java.util.Objects.hash

fun main() {
    println("ADAPTORS")

    val aBob = ABob(2, "apple")
    println(
        """
        $aBob vs ${aBob.toAdaptedBob()}
        IF INLINED, WHY ARE TYPES DIFFERENT?
        ${aBob.javaClass} vs ${aBob.toAdaptedBob().javaClass}
        calling -- ${aBob.toAdaptedBob().foo()}
        equals? ${aBob.toAdaptedBob() == aBob.toAdaptedBob()}
        hashCode? ${aBob.toAdaptedBob().hashCode() == aBob.toAdaptedBob()
            .hashCode()}
    """.trimIndent()
    )
}

class ABob(val a: Int, val b: String) {
    override fun equals(other: Any?) = this === other ||
            other is ABob &&
            a == other.a &&
            b == other.b

    override fun hashCode() = hash(a, b)
    override fun toString() = "BOB: $a & $b"
}

interface Fooable {
    fun foo(): String
}

inline class AdaptedBob(private val bob: ABob) : Fooable {
    override fun foo() = "Foo! ($bob)"

    override fun toString() = bob.toString()
}

fun ABob.toAdaptedBob() = AdaptedBob(this)
