package x.scratch

import x.scratch.units.div
import x.scratch.units.english.Barleycorns
import x.scratch.units.english.EnglishLengths
import x.scratch.units.english.Fathoms
import x.scratch.units.english.Feet
import x.scratch.units.english.Hands
import x.scratch.units.english.Inches
import x.scratch.units.english.Poppyseed
import x.scratch.units.english.Poppyseeds
import x.scratch.units.english.Sticks
import x.scratch.units.english.Yards
import x.scratch.units.english.minus
import x.scratch.units.english.plus
import x.scratch.units.english.poppyseed
import x.scratch.units.english.to
import x.scratch.units.over
import x.scratch.units.times
import x.scratch.units.unaryMinus
import x.scratch.units.unaryPlus
import java.math.BigInteger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.math.abs
import kotlin.math.cos
import kotlin.random.Random
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.typeOf
import kotlin.time.ExperimentalTime
import kotlin.time.TestTimeSource
import kotlin.time.TimeSource
import kotlin.time.measureTimedValue
import kotlin.time.seconds

class A(val p: Int)

@OptIn(
    ExperimentalStdlibApi::class,
    ExperimentalTime::class,
    ExperimentalUnsignedTypes::class
)
fun main() {
    println()
    println("MATHS")

    println(
        "GCD pos to pos: ${BigInteger.valueOf(3).gcd(BigInteger.valueOf(2))}"
    )
    println(
        "GCD pos to neg: ${BigInteger.valueOf(3).gcd(BigInteger.valueOf(-2))}"
    )
    println(
        "GCD neg to pos: ${BigInteger.valueOf(-3).gcd(BigInteger.valueOf(2))}"
    )
    println(
        "GCD neg to neg: ${BigInteger.valueOf(-3)
            .gcd(BigInteger.valueOf(-2))}"
    )

    println("Rounding a positive fraction: ${3 / 2}")
    println("Rounding a negative fraction in the numerator: ${-3 / 2}")
    println("Rounding a negative fraction in the denominator: ${3 / -2}")
    println("Rounding a negative fraction: ${-3 / -2}")

    println()
    println("UNSIGNED TYPES")

    val a: UByte = 3u
    val b: UByte = 5u
    println(a.and(b))
    println(a.or(b))
    println(a.inv())
    println(a.xor(b))

    println()
    println("TYPES AND REFLECTION")

    val intType = typeOf<Int>()
    println(intType)

    accessReifiedTypeArg<String>()
    accessReifiedTypeArg<List<String>>()

    val first = InlineClass(1)
    val second = InlineClass(1)
    println(first == second)
    println(first::class)

    // TODO: Why did this stop working?
//    println(::a.get())
//    println(::a.name)
//    println(::a.javaField)
    println(A(p = 3)::p.get())
    println(A(p = 5)::p.javaField)
    println(::A.javaConstructor)

    foo(second)

    val x: UByte = 255u

    println("$x -> ${x.toByte()}")

    val keys = 'a'..'f'
    val map = keys.associateWith {
        it.toString().capitalize().repeat(3)
    }
    println(map)

    val ic = WrappedInt(3)

    println(ic * 2)

    val mappy = ViewMapAsProperties(mutableMapOf("a" to "apple"))
    println(mappy.a)
    mappy.a = "aardvark"
    println(mappy.a)

    val dotdot = 1..2
    println("$dotdot -> ${dotdot::class}")

    val xs = String::class.supertypes
    println("$xs, ie, ${xs.first()::class}")
    val xxs = String::class.superclasses
    println("$xxs, ie, ${xxs.first()::class}")

    println()
    println("TIME")

    val clock = TimeSource.Monotonic
    val mark1 = clock.markNow()
    println("${mark1.elapsedNow()}")

    val (result, duration) = clock.measureTimedValue {
        "zippo"
    }
    println("$result took $duration")

    val tc = TestTimeSource()
    val tm = tc.markNow()
    tc.plusAssign(1.seconds)
    println("Test clock advanced since mark by ${tm.elapsedNow()}")

    println(Bob(3).aaa(listOf("fruits")))

    println()
    println("NAN")

    println("Kotlin floating point is Java's")
    println("NaN comparison: ${Double.NaN == Double.NaN}")
    println("NaN check: ${Double.NaN.isNaN()}")

    // How to trigger IntelliJ suggestion use of sequence?
    val qqq = listOf("a", "b", "c").filter {
        it == "a"
    }.map {
        1
    }.takeLast(2).forEach {
        println(it)
    }
    println(qqq)

    generateSequence {
        Random.nextInt()
    }.filter {
        it in 0..999
    }.take(2).forEach {
        println(it)
    }

    val nextGen = generate<Int, Int> {
        // TODO: How to use "param" -- 100 vs 1000
        println("it: $it")
        while (true) {
            val next = Random.nextInt()
            if (next in 0..it)
                yield(next)
        }
    }
    println(nextGen.next(100))
    println(nextGen.next(1000))

    val firstName = FirstName("Brian")
    println(firstName)
    println(firstName.name)

    println("Converging at $ALPHA")
    println("Summing is ${sumCos(1.0)}")

    println()
    println("VECTORS")

    val rv0 = RowVector2.of(1, 2)
    println(rv0)
    println(rv0.transpose())
    val cv0 = ColVector2.of(3, 4)
    println(cv0)
    println(cv0.transpose())
    println(rv0 * cv0)
    println(cv0 * rv0)

    println()
    println("OVERFLOW/UNDERFLOW")

    println(
        """
        ${Int.MAX_VALUE} vs ${Int.MAX_VALUE + 1}
        ${Int.MIN_VALUE} vs ${Int.MIN_VALUE - 1}
        ${5 / 3} vs ${5 % 3}
    """.trimIndent()
    )

    println()
    println("ADAPTERS")

    val aBob = ABob(2, "apple")
    println(
        """
        $aBob vs ${aBob.toAdaptedBob()}
        ${aBob.toAdaptedBob().foo()}
        equals? ${aBob.toAdaptedBob() == aBob.toAdaptedBob()}
        hashCode? ${aBob.toAdaptedBob().hashCode() == aBob.toAdaptedBob()
            .hashCode()}
    """.trimIndent()
    )

    println()
    println("UNITS AND MEASURES")

    val m1 = 120.poppyseed

    println(+m1)
    println(-m1)
    println(m1.to(Poppyseeds))
    println(m1.to(Barleycorns))
    println(m1 + m1)
    println(m1 + m1.to(Barleycorns))
    println(m1.to(Hands) - m1)
    println(m1 * 3)
    println(m1 / 3)
    println(m1.to(Inches).to(Sticks))
    println((m1.to(Yards) + m1.to(Feet) - m1.to(Barleycorns)).to(Fathoms))

    println()
    println("BUILDERS")

    val bs = buildString {
        append("first")
        append(' ')
        append("last")
    }
    println(bs)
    val bl = buildList {
        add(0)
        add(3)
    }
    println(bl)

    println()
    println("SCAN")

    val scanMe = 1..7
    val scanned = scanMe.asSequence().scanIndexed(0) { index, acc, elem ->
        (acc + elem) * index
    }
    println("BEFORE: ${scanMe.toList()}")
    println("AFTER: ${scanned.toList()}")

    println()
    println("SEALED CLASSES")

    println("Sealed? ${X::class.isSealed}")
    println("Types: ${X::class.sealedSubclasses.map { it.simpleName }}")
    println(
        "English length types: ${EnglishLengths::class.sealedSubclasses.map {
            it.simpleName
        }}"
    )
}

private sealed class X(val s: String)
private class XA : X("XA")
private data class XB(val a: Int) : X("XB")

const val EPSILON = 1e-16
val ALPHA = alpha()

fun alpha(): Double {
    var x = 1.0
    var cx = cos(x)
    while (abs(x - cx) > EPSILON) {
        x = cx
        cx = cos(x)
    }
    return x
}

/** See https://www.johndcook.com/blog/2020/01/18/variation-on-cosine-fixed-point/ */
fun sumCos(_x: Double): Double {
    var x = _x
    var s = 0.0
    var cx = cos(x)
    var delta = ALPHA - cx
    while (abs(delta) > EPSILON) {
        s += delta
        x = cx
        cx = cos(cx)
        delta = ALPHA - cx
    }
    return s
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> accessReifiedTypeArg() {
    val kType = typeOf<T>()
    println(kType.toString())
}

inline class InlineClass(val value: Int)

@OptIn(ExperimentalContracts::class)
fun synchronize(lock: Any?, block: () -> Unit) {
    // It tells compiler:
    // "This function will invoke 'block' here and now, and exactly one time"
    contract { callsInPlace(block, EXACTLY_ONCE) }

    block()
}

fun foo(lock: Any) {
    val x: Int
    synchronize(lock) {
        x = 42 // Compiler knows that lambda passed to 'synchronize' is called
        // exactly once, so no reassignment is reported
    }
    println(x) // Compiler knows that lambda will be definitely called, performing
    // initialization, so 'x' is considered to be initialized here
}

inline class WrappedInt(val value: Int) {
    operator fun times(b: Int) = WrappedInt(value * b)
}

class ViewMapAsProperties(map: MutableMap<String, Any?>) {
    var a: String by map
}

interface Listy {
    // This extends List only within scope of implementers of the interface
    fun <T> List<T>.doodah() {
        println("Howdy, there, pardner!")
    }
}

data class Bob(val a: Int) : Listy {
    fun <T> aaa(list: List<T>) {
        list.doodah()
    }
}

inline class FirstName(val name: String)
