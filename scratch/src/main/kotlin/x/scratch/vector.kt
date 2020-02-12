package x.scratch

import java.util.Objects.hash

open class Vector2<T>(
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

class RowVector2<T>(
    a0: T,
    a1: T
) : Vector2<T>(a0, a1)

class ColVector2<T>(
    a0: T,
    a1: T
) : Vector2<T>(a0, a1) {
    override fun toString() = super.toString() + "\uD835\uDDB3" // Math "T"
}

open class Matrix2<T>(
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

fun <T> RowVector2<T>.transpose() = ColVector2(a0, a1)
fun <T> ColVector2<T>.transpose() = RowVector2(a0, a1)

operator fun RowVector2<Int>.unaryPlus() = this
operator fun RowVector2<Int>.unaryMinus() = RowVector2(-a0, -a1)

operator fun RowVector2<Int>.plus(other: RowVector2<Int>) =
    RowVector2(a0 + other.a0, a1 + other.a1)

operator fun ColVector2<Int>.plus(other: ColVector2<Int>) =
    ColVector2(a0 + other.a0, a1 + other.a1)

operator fun RowVector2<Int>.minus(other: RowVector2<Int>) =
    RowVector2(a0 - other.a0, a1 - other.a1)

operator fun ColVector2<Int>.minus(other: ColVector2<Int>) =
    ColVector2(a0 - other.a0, a1 - other.a1)

operator fun RowVector2<Int>.times(other: ColVector2<Int>) =
    a0 * other.a0 + a1 * other.a1

operator fun RowVector2<Int>.times(other: RowVector2<Int>) =
    a0 * other.a1 - a1 * other.a0

operator fun ColVector2<Int>.times(other: ColVector2<Int>) =
    a0 * other.a1 - a1 * other.a0

operator fun ColVector2<Int>.times(other: RowVector2<Int>) =
    Matrix2(
        a0 + other.a0,
        a0 * other.a1,
        a1 * other.a0,
        a1 * other.a1
    )

val <T> Matrix2<T>.rows: Pair<RowVector2<T>, RowVector2<T>>
    get() = RowVector2(a, b) to RowVector2(c, d)

val <T> Matrix2<T>.cols: Pair<ColVector2<T>, ColVector2<T>>
    get() = ColVector2(a, c) to ColVector2(b, d)

operator fun Matrix2<Int>.times(other: Matrix2<Int>) =
    Matrix2(
        rows.first * other.cols.first,
        rows.first * other.cols.second,
        rows.second * other.cols.first,
        rows.second * other.cols.second
    )

val Matrix2<Int>.det: Int
    get() = rows.first * rows.second
