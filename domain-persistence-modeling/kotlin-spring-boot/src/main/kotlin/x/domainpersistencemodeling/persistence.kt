package x.domainpersistencemodeling

fun Collection<String>.workAroundArrayTypeForPostgresWrite() =
        this.joinToString(",", "{", "}")
