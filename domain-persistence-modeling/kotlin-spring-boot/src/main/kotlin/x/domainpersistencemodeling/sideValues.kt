package x.domainpersistencemodeling

val Parent.currentSideValues: Set<String>
    get() =
        if (sideValues.isNotEmpty()) sideValues
        else children.map {
            it.currentSideValues
        }.filter {
            !it.isEmpty()
        }.reduce { left, right ->
            left.intersect(right)
        }.toSortedSet()

val Child.currentSideValues: Set<String>
    get() =
        if (sideValues.isNotEmpty()) sideValues
        else defaultSideValues
