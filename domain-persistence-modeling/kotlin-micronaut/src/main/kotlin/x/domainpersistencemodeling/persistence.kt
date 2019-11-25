package x.domainpersistencemodeling

import java.util.*

fun Collection<String>.workAroundArrayTypeForPostgresWrite() =
        this.joinToString(",", "{", "}")

fun Collection<String>.workAroundArrayTypeForPostgresRead(): MutableSet<String> {
    val x = first().removeSurrounding("{", "}")
    return if (x.isEmpty()) TreeSet()
    else TreeSet(x.split(','))
}
