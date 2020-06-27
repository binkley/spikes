package x.scratch

fun main() {
    // See https://en.wikipedia.org/wiki/Primality_test#Pseudocode
    println("==PRIMALITY TESTING")

    for (n in 2L..1_000_000L)
        if (n.isPrime()) println(n)
}

private fun Long.isPrime(): Boolean {
    if (this <= 3L) return this > 1L
    else if (0L == this % 2L || 0L == this % 3L) return false

    var i = 5L

    while (i * i <= this) {
        if (0L == this % i || 0L == this % (i + 2L)) return false
        i += 6L
    }

    return true
}
