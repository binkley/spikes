package x.scratch

fun main() {
    println("== PERMUTATIONS")
    println(
        "${arrayOf(1, 2, 3).contentToString()} -> ${arrayOf(
            1, 2, 3
        ).signature}"
    )
    println(
        "${arrayOf(2, 1, 3).contentToString()} -> ${arrayOf(
            2, 1, 3
        ).signature}"
    )
    println(
        "${arrayOf(2, 3, 1).contentToString()} -> ${arrayOf(
            2, 3, 1
        ).signature}"
    )
}

val Array<Int>.signature: Int
    get() {
        var inversions = 0
        for (i in 0 until size) {
            val n = this[i]
            for (j in i until size)
                if (n > this[j]) ++inversions
        }
        return if (0 == inversions % 2) 1 else -1
    }
