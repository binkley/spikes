package x.domainpersistencemodeling

import java.util.*

fun Collection<String>.workAroundArrayTypeForPostgresWrite() =
        this.joinToString(",", "{", "}")

fun Collection<String>.workAroundArrayTypeForPostgresRead(): MutableSet<String> =
        TreeSet(first().removeSurrounding("{", "}").split(','))
