package x.domainpersistencemodeling

val <T> T.currentSideValues: Set<String>
        where T : ParentSimpleDetails,
              T : ParentComputedDetails
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

val ChildSimpleDetails.currentSideValues: Set<String>
    get() = when {
        !relevant -> setOf()
        sideValues.isNotEmpty() -> sideValues
        else -> defaultSideValues
    }

val Set<ChildSimpleDetails>.at
    get() = map {
        it.at
    }.min()
