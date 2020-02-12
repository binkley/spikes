package x.scratch

import java.util.Objects.hash

interface Arithmetic<T : Arithmetic<T>> {
    @Suppress("UNCHECKED_CAST")
    operator fun unaryPlus(): T = this as T
    operator fun unaryMinus(): T
    operator fun plus(other: T): T
    operator fun minus(other: T): T = this + -other
    operator fun times(other: T): T
}

inline class MathInt(val value: Int) : Arithmetic<MathInt> {
    override fun unaryMinus() = MathInt(-value)
    override fun plus(other: MathInt) = MathInt(value + other.value)
    override fun times(other: MathInt) = MathInt(value * other.value)

    override fun toString() = value.toString()
}

open class Vector2<T : Arithmetic<T>>(
    val a0: T,
    val a1: T
) {
    operator fun component0() = a0
    operator fun component1() = a1

    override fun equals(other: Any?) = this === other ||
            other is Vector2<*> &&
            a0 == other.a0 &&
            a1 == other.a1

    override fun hashCode() = hash(a0, a1)
    override fun toString() = "[$a0, $a1]"
}

class RowVector2<T : Arithmetic<T>>(
    a0: T,
    a1: T
) : Vector2<T>(a0, a1) {
    companion object {
        fun of(a0: Int, a1: Int) =
            RowVector2(MathInt(a0), MathInt(a1))
    }
}

class ColVector2<T : Arithmetic<T>>(
    a0: T,
    a1: T
) : Vector2<T>(a0, a1) {
    override fun toString() = super.toString() + "\uD835\uDDB3" // Math "T"

    companion object {
        fun of(a0: Int, a1: Int) =
            ColVector2(MathInt(a0), MathInt(a1))
    }
}

open class Matrix2<T : Arithmetic<T>>(
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

    override fun hashCode() = hash(a, b, c, d)
    override fun toString() = "[$a, $b; $c, $d]"
}

fun <T : Arithmetic<T>> RowVector2<T>.transpose() = ColVector2(a0, a1)
fun <T : Arithmetic<T>> ColVector2<T>.transpose() = RowVector2(a0, a1)

operator fun <T : Arithmetic<T>> RowVector2<T>.unaryPlus() = this
operator fun <T : Arithmetic<T>> RowVector2<T>.unaryMinus() =
    RowVector2(-a0, -a1)

operator fun <T : Arithmetic<T>> RowVector2<T>.plus(other: RowVector2<T>) =
    RowVector2(a0 + other.a0, a1 + other.a1)

operator fun <T : Arithmetic<T>> ColVector2<T>.plus(other: ColVector2<T>) =
    ColVector2(a0 + other.a0, a1 + other.a1)

operator fun <T : Arithmetic<T>> RowVector2<T>.minus(other: RowVector2<T>) =
    RowVector2(a0 - other.a0, a1 - other.a1)

operator fun <T : Arithmetic<T>> ColVector2<T>.minus(other: ColVector2<T>) =
    ColVector2(a0 - other.a0, a1 - other.a1)

operator fun <T : Arithmetic<T>> RowVector2<T>.times(other: ColVector2<T>) =
    a0 * other.a0 + a1 * other.a1

operator fun <T : Arithmetic<T>> RowVector2<T>.times(other: RowVector2<T>) =
    a0 * other.a1 - a1 * other.a0

operator fun <T : Arithmetic<T>> ColVector2<T>.times(other: ColVector2<T>) =
    a0 * other.a1 - a1 * other.a0

operator fun <T : Arithmetic<T>> ColVector2<T>.times(other: RowVector2<T>) =
    Matrix2(
        a0 + other.a0,
        a0 * other.a1,
        a1 * other.a0,
        a1 * other.a1
    )

val <T : Arithmetic<T>> Matrix2<T>.rows: Pair<RowVector2<T>, RowVector2<T>>
    get() = RowVector2(a, b) to RowVector2(c, d)

val <T : Arithmetic<T>> Matrix2<T>.cols: Pair<ColVector2<T>, ColVector2<T>>
    get() = ColVector2(a, c) to ColVector2(b, d)

operator fun <T : Arithmetic<T>> Matrix2<T>.times(other: Matrix2<T>) =
    Matrix2(
        rows.first * other.cols.first,
        rows.first * other.cols.second,
        rows.second * other.cols.first,
        rows.second * other.cols.second
    )

val <T : Arithmetic<T>> Matrix2<T>.det: T
    get() = rows.first * rows.second
