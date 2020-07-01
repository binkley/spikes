package x.scratch

data class Complex(val a: Int, val b: Int) : Ring<Complex> {
    override val companion = ComplexCompanion
    override fun unaryMinus(): Complex = Complex(-a, -b)
    override fun plus(addend: Complex): Complex =
        Complex(a + addend.a, b + addend.b)

    override fun times(multiplicand: Complex): Complex = Complex(
        a * multiplicand.a - b * multiplicand.b,
        a * multiplicand.b + b * multiplicand.a
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
        override val ZERO = Complex(0, 0)
        override val ONE = Complex(1, 0)
        val I = Complex(0, 1)
    }
}
