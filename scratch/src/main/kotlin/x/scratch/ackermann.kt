package x.scratch

import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.ZERO

/**
 * @todo https://adamschoenemann.dk/posts/2019-02-12-trampolines.html for
 *       trampolines to avoid stack overflow
 */
fun main() {
    println("==ACKERMANN")
    for (m in 0..3) // 4 overflows the stack! :)
        for (n in 0..3)
            println(
                "$m,$n -> ${ackermann(m.toBigInteger(), n.toBigInteger())}"
            )
}

tailrec fun ackermann(m: BigInteger, n: BigInteger): BigInteger =
    when (ZERO) {
        m -> n + ONE
        n -> ackermann(m - ONE, ONE)
        else -> ackermann(m - ONE, ackermann(m, n - ONE))
    }
