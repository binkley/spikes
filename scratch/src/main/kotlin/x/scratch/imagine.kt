package x.scratch

fun main() {
    println("== IMAGINARY AND COMPLEX NUMBERS")
    println("i -> $I")
    println("i::class -> ${I::class}")
    println("i.javaClass -> ${I.javaClass}")
    println(".real -> ${I.toInt()}")
    val z = 1 + 1.i
    println("z -> $z")
    println(".real -> ${z.real}")
    println(".imag -> ${z.imag}")
    println("z* -> ${z.conjugate}")
    println("+z -> ${+z}")
    println("-z -> ${-z}")
    println("z + z -> ${z + z}")
    println("z + 1 -> ${z + 1}")
    println("1 + z -> ${1 + z}")
    println("z + 1i -> ${z + 1.i}")
    println("1i + z -> ${1.i + z}")
    println("z - z -> ${z - z}")
    println("z - 1 -> ${z - 1}")
    println("1 - z -> ${1 - z}")
    println("z - 1i -> ${z - 1.i}")
    println("1i - z -> ${1.i - z}")
    println("z * z -> ${z * z}")
    println("z * 2 -> ${z * 2}")
    println("2 * z -> ${2 * z}")
    println("z * 2i -> ${z * 2.i}")
    println("2i * z -> ${2.i * z}")
    println("(1+0i).toInt() -> ${(1 + 0.i).toInt()}")
    println("(0+1i).toImaginary() -> ${(0 + 1.i).toImaginary()}")
}

inline class Imaginary(val real: Int) {
    override fun toString() = "${real}i"
}

val I = 1.i
fun Int.toImaginary() = Imaginary(this)
val Int.i get() = toImaginary()
fun Imaginary.toInt() = real

operator fun Imaginary.unaryPlus() = this
operator fun Imaginary.unaryMinus() = (-real).toImaginary()

operator fun Imaginary.plus(addend: Imaginary) =
    (real + addend.real).toImaginary()

operator fun Imaginary.minus(subtrahend: Imaginary) =
    (real - subtrahend.real).toImaginary()

operator fun Imaginary.times(multiplicand: Imaginary) =
    -(real * multiplicand.real)

operator fun Imaginary.times(multiplicand: Int) =
    (real * multiplicand).toImaginary()

operator fun Int.times(multiplicand: Imaginary) = multiplicand * this

data class Complex(val real: Int, val imag: Imaginary) {
    override fun toString() =
        if (imag.real < 0) "$real-${-imag}" else "$real+$imag"
}

operator fun Int.plus(imag: Imaginary) = Complex(this, imag)
operator fun Imaginary.plus(real: Int) = real + this
operator fun Int.minus(imag: Imaginary) = this + -imag
operator fun Imaginary.minus(real: Int) = real + -this

val Complex.conjugate get() = real + -imag

operator fun Complex.unaryPlus() = this
operator fun Complex.unaryMinus() = Complex(-real, -imag)

operator fun Complex.plus(addend: Complex) =
    (real + addend.real) + (imag + addend.imag)

operator fun Complex.plus(addend: Int) =
    (real + addend) + imag

operator fun Int.plus(addend: Complex) =
    (this + addend.real) + addend.imag

operator fun Complex.plus(addend: Imaginary) =
    real + (imag + addend)

operator fun Imaginary.plus(addend: Complex) =
    addend.real + (this + addend.imag)

operator fun Complex.minus(subtrahend: Complex) = this + -subtrahend
operator fun Complex.minus(subtrahend: Int) = this + -subtrahend
operator fun Int.minus(subtrahend: Complex) = this + -subtrahend
operator fun Complex.minus(subtrahend: Imaginary) = this + -subtrahend
operator fun Imaginary.minus(subtrahend: Complex) = this + -subtrahend

operator fun Complex.times(multiplicand: Complex) =
    (real * multiplicand.real + imag * multiplicand.imag) +
            (real * multiplicand.imag + imag * multiplicand.real)

operator fun Complex.times(multiplicand: Int) =
    real * multiplicand + imag * multiplicand

operator fun Int.times(multiplicand: Complex) = multiplicand * this

operator fun Complex.times(multiplicand: Imaginary) =
    real * multiplicand + imag * multiplicand

operator fun Imaginary.times(multiplicand: Complex) = multiplicand * this

fun Complex.toInt() =
    if (0.i == imag) real else throw ArithmeticException("Not real")

fun Complex.toImaginary() =
    if (0 == real) imag else throw ArithmeticException("Not imaginary")
