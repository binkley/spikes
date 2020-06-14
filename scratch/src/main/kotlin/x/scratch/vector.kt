package x.scratch

import java.util.Objects.hash

fun main() {
    println("VECTORS")

    val rv0 = RowVector2.of(1, 2)
    println("ROW VECTOR - $rv0")
    println("TRANSPOSE - ${rv0.transpose()}")
    val cv0 = ColVector2.of(3, 4)
    println("COL VECTOR - $cv0")
    println("TRANSPOSE - ${cv0.transpose()}")
    println("ROW * COL - ${rv0 * cv0}")
    val mat0 = cv0 * rv0
    println("COL * ROW - $mat0")
    println("DET - ${mat0.det}")
}

interface GroupCompanion<T : Group<T>> {
    val ZERO: T
}

interface Group<T : Group<T>> {
    val companion: GroupCompanion<T>

    @Suppress("UNCHECKED_CAST")
    operator fun unaryPlus(): T = this as T
    operator fun unaryMinus(): T
    operator fun plus(other: T): T
    operator fun minus(other: T): T = this + -other
}

interface RingCompanion<T : Ring<T>> : GroupCompanion<T> {
    val UNIT: T
}

interface Ring<T : Ring<T>> : Group<T> {
    override val companion: RingCompanion<T>

    operator fun times(other: T): T
}

inline class MathInt(val value: Int) : Ring<MathInt> {
    override val companion: MathIntCompanion get() = MathIntCompanion

    override fun unaryMinus() = MathInt(-value)
    override fun plus(other: MathInt) = MathInt(value + other.value)
    override fun times(other: MathInt) = MathInt(value * other.value)

    override fun toString() = value.toString()

    companion object MathIntCompanion : RingCompanion<MathInt> {
        override val ZERO = MathInt(0)
        override val UNIT = MathInt(1)
    }
}

open class Vector2Base<T : Ring<T>>(
    val a0: T,
    val a1: T
) {
    operator fun component0() = a0
    operator fun component1() = a1
}

class RowVector2<T : Ring<T>>(
    a0: T,
    a1: T
) : Vector2Base<T>(a0, a1) {
    override fun equals(other: Any?) = this === other ||
            other is RowVector2<*> &&
            a0 == other.a0 &&
            a1 == other.a1

    override fun hashCode() = hash(javaClass, a0, a1)
    override fun toString() = "[$a0, $a1]"

    companion object {
        fun of(a0: Int, a1: Int) =
            RowVector2(MathInt(a0), MathInt(a1))
    }
}

class ColVector2<T : Ring<T>>(
    a0: T,
    a1: T
) : Vector2Base<T>(a0, a1) {
    override fun equals(other: Any?) = this === other ||
            other is ColVector2<*> &&
            a0 == other.a0 &&
            a1 == other.a1

    override fun hashCode() = hash(javaClass, a0, a1)
    override fun toString() = "[$a0; $a1]"

    companion object {
        fun of(a0: Int, a1: Int) =
            ColVector2(MathInt(a0), MathInt(a1))
    }
}

open class Matrix2<T : Ring<T>>(
    val a: T,
    val b: T,
    val c: T,
    val d: T
) {
    operator fun component0() = a
    operator fun component1() = b
    operator fun component2() = c
    operator fun component3() = d

    override fun equals(other: Any?) = this === other ||
            other is Matrix2<*> &&
            a == other.a &&
            b == other.b &&
            c == other.c &&
            d == other.d

    override fun hashCode() = hash(javaClass, a, b, c, d)
    override fun toString() = "[$a, $b; $c, $d]"
}

fun <T : Ring<T>> RowVector2<T>.transpose() = ColVector2(a0, a1)
fun <T : Ring<T>> ColVector2<T>.transpose() = RowVector2(a0, a1)

operator fun <T : Ring<T>> RowVector2<T>.unaryPlus() = this
operator fun <T : Ring<T>> RowVector2<T>.unaryMinus() =
    RowVector2(-a0, -a1)

operator fun <T : Ring<T>> RowVector2<T>.plus(other: RowVector2<T>) =
    RowVector2(a0 + other.a0, a1 + other.a1)

operator fun <T : Ring<T>> ColVector2<T>.plus(other: ColVector2<T>) =
    ColVector2(a0 + other.a0, a1 + other.a1)

operator fun <T : Ring<T>> RowVector2<T>.minus(other: RowVector2<T>) =
    RowVector2(a0 - other.a0, a1 - other.a1)

operator fun <T : Ring<T>> ColVector2<T>.minus(other: ColVector2<T>) =
    ColVector2(a0 - other.a0, a1 - other.a1)

operator fun <T : Ring<T>> RowVector2<T>.times(other: ColVector2<T>) =
    a0 * other.a0 + a1 * other.a1

operator fun <T : Ring<T>> RowVector2<T>.times(other: RowVector2<T>) =
    a0 * other.a1 - a1 * other.a0

operator fun <T : Ring<T>> ColVector2<T>.times(other: ColVector2<T>) =
    a0 * other.a1 - a1 * other.a0

operator fun <T : Ring<T>> ColVector2<T>.times(other: RowVector2<T>) =
    Matrix2(
        a0 + other.a0,
        a0 * other.a1,
        a1 * other.a0,
        a1 * other.a1
    )

val <T : Ring<T>> Matrix2<T>.rows: Pair<RowVector2<T>, RowVector2<T>>
    get() = RowVector2(a, b) to RowVector2(c, d)

val <T : Ring<T>> Matrix2<T>.cols: Pair<ColVector2<T>, ColVector2<T>>
    get() = ColVector2(a, c) to ColVector2(b, d)

operator fun <T : Ring<T>> Matrix2<T>.times(other: Matrix2<T>) =
    Matrix2(
        rows.first * other.cols.first,
        rows.first * other.cols.second,
        rows.second * other.cols.first,
        rows.second * other.cols.second
    )

val <T : Ring<T>> Matrix2<T>.det: T
    get() = rows.first * rows.second
