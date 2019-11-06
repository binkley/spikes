package x.domainpersistencemodeling

val Parent.currentSideValues: Set<String>
    get() = when {
        sideValues.isNotEmpty() -> sideValues
        else -> children.map {
            it.currentSideValues
        }.filter {
            it.isNotEmpty()
        }.reduce { left, right ->
            left.intersect(right)
        }.toSortedSet()
    }

val Child.currentSideValues: Set<String>
    get() = when {
        !relevant -> setOf()
        sideValues.isNotEmpty() -> sideValues
        else -> defaultSideValues
    }
