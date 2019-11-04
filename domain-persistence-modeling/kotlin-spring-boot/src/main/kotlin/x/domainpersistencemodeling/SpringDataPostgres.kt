package x.domainpersistencemodeling

fun Collection<*>.workAroundArrayTypeForPostgres() =
        this.joinToString(",", "{", "}")
