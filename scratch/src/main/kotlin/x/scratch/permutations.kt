package x.scratch

fun main() {
    println("== PERMUTATIONS")
    println(
        "SIGNATURE ${arrayOf(1, 2, 3).contentToString()} -> ${arrayOf(
            1, 2, 3
        ).signature}"
    )
    println(
        "SIGNATURE ${arrayOf(2, 1, 3).contentToString()} -> ${arrayOf(
            2, 1, 3
        ).signature}"
    )
    println(
        "SIGNATURE ${arrayOf(2, 3, 1).contentToString()} -> ${arrayOf(
            2, 3, 1
        ).signature}"
    )
    println(
        "SIGNATURE ${arrayOf(1, 1, 1).contentToString()} -> ${arrayOf(
            1, 1, 1
        ).signature}"
    )
    println(
        "LEVI-CEVITA ${arrayOf(1, 2, 3).contentToString()} -> ${arrayOf(
            1, 2, 3
        ).leviCevita}"
    )
    println(
        "LEVI-CEVITA ${arrayOf(2, 1, 3).contentToString()} -> ${arrayOf(
            2, 1, 3
        ).leviCevita}"
    )
    println(
        "LEVI-CEVITA ${arrayOf(2, 3, 1).contentToString()} -> ${arrayOf(
            2, 3, 1
        ).leviCevita}"
    )
    println(
        "LEVI-CEVITA ${arrayOf(1, 1, 1).contentToString()} -> ${arrayOf(
            1, 1, 1
        ).leviCevita}"
    )
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
