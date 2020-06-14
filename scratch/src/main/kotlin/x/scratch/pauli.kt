package x.scratch

import x.scratch.Pauli.Companion.sigmaI
import x.scratch.Pauli.Companion.sigmaX
import x.scratch.Pauli.Companion.sigmaY
import x.scratch.Pauli.Companion.sigmaZ

fun main() {
    println("== PAULI")
    println("σ₀ -> ${sigmaI.toFullString()}")
    println("σ₁ -> ${sigmaX.toFullString()}")
    println("σ₁² -> ${sigmaX * sigmaX}")
    println("σ₁σ₂ -> ${sigmaX * sigmaY} (-σ₂σ₁ -> ${-sigmaY * sigmaX})")
    println("σ₁σ₃ -> ${sigmaX * sigmaZ}")
    println("σ₂ -> ${sigmaY.toFullString()}")
    println("σ₂² -> ${sigmaY * sigmaY}")
    println("σ₂σ₁ -> ${sigmaY * sigmaX}")
    println("σ₂σ₃ -> ${sigmaY * sigmaZ}")
    println("σ₃ -> ${sigmaZ.toFullString()}")
    println("σ₃² -> ${sigmaZ * sigmaZ}")
    println("σ₃σ₁ -> ${sigmaZ * sigmaX}")
    println("σ₃σ₂ -> ${sigmaZ * sigmaY}")
}

private val Z = Complex(0, 0)
private val U = Complex(1, 0)
private val i = Complex(0, 1)

data class Complex(val a: Int, val b: Int) : Ring<Complex> {
    override val companion = ComplexCompanion
    override fun unaryMinus(): Complex = Complex(-a, -b)
    override fun plus(other: Complex): Complex =
        Complex(a + other.a, b + other.b)

    override fun times(other: Complex): Complex = Complex(
        a * other.a - b * other.b,
        a * other.b + b * other.a
    )

    val conjugate: Complex get() = Complex(a, -b)

    override fun toString() = when {
        0 == b -> "$a"
        0 == a -> when (b) {
            1 -> "i"
            -1 -> "-i"
            else -> "${b}i"
        }
        else -> when {
            1 == b -> "$a+i"
            -1 == b -> "$a-i"
            0 > b -> "$a-${b}i"
            else -> "$a+${b}i"
        }
    }

    companion object ComplexCompanion : RingCompanion<Complex> {
        override val additiveIdentity = Z
        override val multiplicativeIdentity = U
    }
}

class Pauli private constructor(
    val n: Int,
    val a: Complex,
    val b: Complex,
    val c: Complex,
    val d: Complex
) {
    val det: Complex get() = a * d - b * c
    val tr: Complex get() = a + d
    val complexConjugate: Pauli get() = -sigmaY * this * sigmaY
    val conjugateTranspose: Pauli
        get() = Pauli(
            -1,
            a.conjugate,
            c.conjugate,
            b.conjugate,
            d.conjugate
        )

    operator fun unaryPlus() = this
    operator fun unaryMinus() = Pauli(-n, -a, -b, -c, -d)

    operator fun times(multiplicand: Pauli): Pauli {
        return Pauli(
            -1,
            a * multiplicand.a + b * multiplicand.c,
            a * multiplicand.b + b * multiplicand.d,
            c * multiplicand.a + d * multiplicand.c,
            c * multiplicand.b + d * multiplicand.d
        )
    }

    override fun toString() = when {
        U == a && Z == b && Z == c && U == d -> "I"
        this == sigmaX -> "σ₁"
        this == sigmaY -> "σ₂"
        this == sigmaZ -> "σ₃"
        else -> "!σₙ${toMatrixString()}"
    }

    fun toMatrixString() = "[$a, $b; $c, $d]"

    fun toFullString() =
        "${toMatrixString()}; det=$det; tr=$tr; c.c.=$complexConjugate, c.t.=$conjugateTranspose"

    companion object {
        val sigmaI = Pauli(0, U, Z, Z, U)
        val sigmaX = sigma(1)
        val sigmaY = sigma(2)
        val sigmaZ = sigma(3)

        private fun sigma(n: Int) = Pauli(
            n,
            Complex(δ(n, 3), 0),
            Complex(δ(n, 1), -δ(n, 2)),
            Complex(δ(n, 1), δ(n, 2)),
            Complex(-δ(n, 3), 0)
        )
    }
}

@Suppress("FunctionName", "NonAsciiCharacters")
private fun δ(a: Int, b: Int) = if (a == b) 1 else 0
