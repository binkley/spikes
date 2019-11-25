package x.domainpersistencemodeling

import java.util.TreeSet

fun Collection<String>.workAroundArrayTypeForPostgresWrite(): String =
    joinToString(",", "{", "}")

fun Collection<String>.workAroundArrayTypeForPostgresRead(): MutableSet<String> {
    val raw = first().removeSurrounding("{", "}")
    return if (raw.isEmpty()) TreeSet()
    else TreeSet(raw.split(','))
}
