package x.scratch

fun main() {
    val f = { s: String -> s.toInt() }
    val g = { i: Int -> "$i" }
    val h = { d: Double -> d.toInt() }

    println((f o g o h)(3.0))
}

infix fun <T, U, R> ((U) -> R).o(g: (T) -> U) = { t: T -> this(g(t)) }
