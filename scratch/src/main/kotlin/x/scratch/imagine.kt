package x.scratch

fun main() {
    println("== IMAGINARY AND COMPLEX NUMBERS")
    println("i -> $I")
    println("i::class -> ${I::class}")
    println("i.javaClass -> ${I.javaClass}")
    println("i.value -> ${I.value}")
    val z = 1 + 1.i
    println("z -> $z")
    println("z.real -> ${z.real}")
    println("z.imag -> ${z.imag}")
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
    println("(1+0i).toLong() -> ${(1 + 0.i).toLong()}")
    println("(1+0i).toInt() -> ${(1 + 0.i).toInt()}")
    try {
        (0 + 1.i).toLong()
    } catch (e: ArithmeticException) {
        println("(0+1i).toLong() -> $e")
    }
    try {
        (0 + 1.i).toInt()
    } catch (e: ArithmeticException) {
        println("(0+1i).toInt() -> $e")
    }
    println("(0+1i).toImaginary() -> ${(0 + 1.i).toImaginary()}")
    try {
        (1 + 0.i).toImaginary()
    } catch (e: ArithmeticException) {
        println("(1+0i).toImaginary() -> $e")
    }
}

inline class Imaginary(val value: Long) {
    override fun toString() = "${value}i"
}

val I = 1L.i
fun Long.toImaginary() = Imaginary(this)
fun Int.toImaginary() = toLong().toImaginary()
val Long.i get() = toImaginary()
val Int.i get() = toImaginary()

operator fun Imaginary.unaryPlus() = this
operator fun Imaginary.unaryMinus() = (-value).toImaginary()

operator fun Imaginary.plus(addend: Imaginary) =
    (value + addend.value).toImaginary()

operator fun Imaginary.minus(subtrahend: Imaginary) =
    (value - subtrahend.value).toImaginary()

operator fun Imaginary.times(multiplicand: Imaginary) =
    -(value * multiplicand.value)

operator fun Imaginary.times(multiplicand: Long) =
    (value * multiplicand).toImaginary()

operator fun Imaginary.times(multiplicand: Int) =
    this * multiplicand.toLong()

operator fun Long.times(multiplicand: Imaginary) = multiplicand * this
operator fun Int.times(multiplicand: Imaginary) = multiplicand * this

data class Complex(val real: Long, val imag: Imaginary) {
    override fun toString() =
        if (imag.value < 0) "$real-${-imag}" else "$real+$imag"
}

operator fun Long.plus(imag: Imaginary) = Complex(this, imag)
operator fun Int.plus(imag: Imaginary) = toLong() + imag
operator fun Imaginary.plus(real: Long) = real + this
operator fun Imaginary.plus(real: Int) = real + this
operator fun Long.minus(imag: Imaginary) = this + -imag
operator fun Int.minus(imag: Imaginary) = this + -imag
operator fun Imaginary.minus(real: Long) = real + -this
operator fun Imaginary.minus(real: Int) = real + -this

val Complex.conjugate get() = real + -imag

operator fun Complex.unaryPlus() = this
operator fun Complex.unaryMinus() = Complex(-real, -imag)

operator fun Complex.plus(addend: Complex) =
    (real + addend.real) + (imag + addend.imag)

operator fun Complex.plus(addend: Long) =
    (real + addend) + imag

operator fun Complex.plus(addend: Int) =
    (real + addend) + imag

operator fun Long.plus(addend: Complex) =
    (this + addend.real) + addend.imag

operator fun Int.plus(addend: Complex) =
    (this + addend.real) + addend.imag

operator fun Complex.plus(addend: Imaginary) =
    real + (imag + addend)

operator fun Imaginary.plus(addend: Complex) =
    addend.real + (this + addend.imag)

operator fun Complex.minus(subtrahend: Complex) = this + -subtrahend
operator fun Complex.minus(subtrahend: Long) = this + -subtrahend
operator fun Complex.minus(subtrahend: Int) = this + -subtrahend
operator fun Long.minus(subtrahend: Complex) = this + -subtrahend
operator fun Int.minus(subtrahend: Complex) = this + -subtrahend
operator fun Complex.minus(subtrahend: Imaginary) = this + -subtrahend
operator fun Imaginary.minus(subtrahend: Complex) = this + -subtrahend

operator fun Complex.times(multiplicand: Complex) =
    (real * multiplicand.real + imag * multiplicand.imag) +
            (real * multiplicand.imag + imag * multiplicand.real)

operator fun Complex.times(multiplicand: Long) =
    real * multiplicand + imag * multiplicand

operator fun Complex.times(multiplicand: Int) =
    real * multiplicand + imag * multiplicand

operator fun Long.times(multiplicand: Complex) = multiplicand * this
operator fun Int.times(multiplicand: Complex) = multiplicand * this

operator fun Complex.times(multiplicand: Imaginary) =
    real * multiplicand + imag * multiplicand

operator fun Imaginary.times(multiplicand: Complex) = multiplicand * this

fun Complex.toLong() =
    if (0L.i == imag) real else throw ArithmeticException("Not real")

fun Complex.toInt() =
    if (0L.i == imag) real.toInt() else throw ArithmeticException("Not real")

fun Complex.toImaginary() =
    if (0L == real) imag else throw ArithmeticException("Not imaginary")
