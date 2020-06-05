package x.scratch

import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.ZERO

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
        else -> ackermann(m - ONE, ackermann0(m, n - ONE))
    }

private fun ackermann0(m: BigInteger, n: BigInteger) = ackermann(m, n)
