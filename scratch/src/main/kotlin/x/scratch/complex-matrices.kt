package x.scratch

import x.scratch.CMatrix2.Companion.I

fun main() {
    println("== COMPLEX NUMBERS WITH MATRICES")
    println("I -> $I")
    println("1+1i -> ${CMatrix2(1, 1)}")
    println("(1+1i)^2 -> ${CMatrix2(1, 1) * CMatrix2(1, 1)}")
}

class CMatrix2(private val x: Int, private val y: Int) {
    val a get() = x
    val b get() = -y
    val c get() = y
    val d get() = x

    val transpose get() = CMatrix2(x, -y)
    val det get() = x * x + y * y

    operator fun unaryPlus() = this
    operator fun unaryMinus() = CMatrix2(-x, -y)

    operator fun plus(addend: CMatrix2) =
        CMatrix2(x + addend.x, y + addend.y)

    operator fun minus(addend: CMatrix2) = this + -addend

    operator fun times(multiplicand: CMatrix2) = CMatrix2(
        x * multiplicand.x - y * multiplicand.y,
        y * multiplicand.x + x * multiplicand.y
    )

    override fun toString() = "[$a, $b; $c; $d]"

    companion object {
        val I = CMatrix2(0, 1)
    }
}
