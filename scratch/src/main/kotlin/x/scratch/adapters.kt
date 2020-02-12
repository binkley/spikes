package x.scratch

import java.util.Objects.hash

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
