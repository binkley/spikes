package x.scratch

import kotlin.reflect.KProperty1

fun main() {
    println("== PERMUTATIONS")
    println()
    demonstrate(Array<Int>::signature)
    println()
    demonstrate(Array<Int>::leviCevita)
}

private fun demonstrate(propRef: KProperty1<Array<Int>, Int>) {
    val header: String = when (propRef) {
        Array<Int>::signature -> "SIGNATURE"
        Array<Int>::leviCevita -> "LEVI-CEVITA"
        else -> error("New method: ${propRef.name}")
    }

    for (a in arrayOf(
        arrayOf(1, 2, 3),
        arrayOf(2, 1, 3),
        arrayOf(2, 3, 1),
        arrayOf(1, 1, 1),
    )) println("$header ${a.contentToString()} -> ${propRef.get(a)}")
}

val Array<Int>.signature: Int
    get() {
        var inversions = 0
        for (i in 0 until size) {
            val n = this[i]
            for (j in i + 1 until size)
                if (n > this[j]) ++inversions
        }
        return if (0 == inversions % 2) 1 else -1
    }

val Array<Int>.leviCevita: Int
    get() {
        var inversions = 0
        for (i in 0 until size) {
            val n = this[i]
            for (j in i + 1 until size) when {
                n == this[j] -> return 0
                n > this[j] -> ++inversions
            }
        }
        return if (0 == inversions % 2) 1 else -1
    }
