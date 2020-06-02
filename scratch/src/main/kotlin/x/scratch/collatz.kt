package x.scratch

import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.ZERO

fun main() {
    println("== COLLATZ")
    for (n in 2..40) {
        val path = path(n, ArrayList(256))
        println(format(n, path))
    }

    println()
    println("CHECKING THAT ALTERNATE MERSENNE NUMBERS ARE DIVISIBLE BY 3...")
    var unexpected = true
    for (p in 1..10000) {
        val divisibleBy3 = divisibleBy3(p)
        if (divisibleBy3 == unexpected) error("Did not alternate: $p")
        unexpected = divisibleBy3
    }
    println("PASSED THROUGH 2^10000-1")
}

private fun format(n: Int, path: List<Int>): String {
    var evens = 0
    var odds = 0
    for (i in path) {
        if (1 == i) break
        if (0 == i % 2) ++evens else ++odds
    }
    val primeFactors = primeFactorize(n)
    val evenOddRatio = "%.1f".format(100 * evens.toDouble() / (evens + odds))

    return "$n -> ${path.size} -> even/odd: $evenOddRatio% ($evens/$odds) -> prime factors: $primeFactors"
}

private tailrec fun path(n: Int, ns: MutableList<Int>): List<Int> {
    ns += n
    if (1 == n) return ns
    return path(next(n), ns)
}

private fun next(n: Int) = when {
    0 == n % 2 -> n / 2
    else -> 3 * n + 1
}

private val firstNPrimes = listOf(
    2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67,
    71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149,
    151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229,
    233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313,
    317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409,
    419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499,
    503, 509, 521, 523, 541, 547, 557, 563, 569, 571, 577, 587, 593, 599, 601,
    607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691,
    701, 709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773, 787, 797, 809,
    811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877, 881, 883, 887, 907,
    911, 919, 929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997, 1009,
    1013
)

/** @todo This is such an _amazingly naive_ implementation */
private fun primeFactorize(n: Int): List<Int> {
    val factors = mutableListOf<Int>()
    for (p in firstNPrimes) {
        if (0 == n % p) factors += p
    }
    return factors
}

private val THREE = BigInteger.valueOf(3)
private fun divisibleBy3(p: Int) = ZERO == (pow2x(p) - ONE) % THREE
private fun pow2x(p: Int) = ONE.shiftLeft(p)
