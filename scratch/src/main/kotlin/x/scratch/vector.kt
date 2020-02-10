package x.scratch

import java.util.Objects.hash

open class Vector2<T>(
    val x0: T,
    val x1: T
) {
    operator fun component0() = x0
    operator fun component1() = x1

    override fun equals(other: Any?) = this === other ||
            other is Vector2<*> &&
            x0 == other.x0 &&
            x1 == other.x1

    override fun hashCode() = hash(x0, x1)
    override fun toString() = "[$x0, $x1]"
}

class RowVector2<T>(
    x0: T,
    x1: T
) : Vector2<T>(x0, x1)

class ColVector2<T>(
    x0: T,
    x1: T
) : Vector2<T>(x0, x1) {
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

fun <T> RowVector2<T>.transpose() = ColVector2(x0, x1)
fun <T> ColVector2<T>.transpose() = RowVector2(x0, x1)

operator fun RowVector2<Int>.unaryPlus() = this
operator fun RowVector2<Int>.unaryMinus() = RowVector2(-x0, -x1)

operator fun RowVector2<Int>.plus(other: RowVector2<Int>) =
    RowVector2(x0 + other.x0, x1 + other.x1)

operator fun ColVector2<Int>.plus(other: ColVector2<Int>) =
    ColVector2(x0 + other.x0, x1 + other.x1)

operator fun RowVector2<Int>.minus(other: RowVector2<Int>) =
    RowVector2(x0 - other.x0, x1 - other.x1)

operator fun ColVector2<Int>.minus(other: ColVector2<Int>) =
    ColVector2(x0 - other.x0, x1 - other.x1)

operator fun RowVector2<Int>.times(other: ColVector2<Int>) =
    x0 * other.x0 + x1 * other.x1

operator fun ColVector2<Int>.times(other: RowVector2<Int>) =
    Matrix2(
        x0 + other.x0,
        x0 * other.x1,
        x1 * other.x0,
        x1 * other.x1
    )
