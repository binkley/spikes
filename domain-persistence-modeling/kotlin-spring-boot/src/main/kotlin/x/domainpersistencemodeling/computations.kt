package x.domainpersistencemodeling

val <T> T.currentSideValues: Set<String>
        where T : ParentIntrinsicDetails,
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

val ChildIntrinsicDetails.currentSideValues: Set<String>
    get() = when {
        !relevant -> setOf()
        sideValues.isNotEmpty() -> sideValues
        else -> defaultSideValues
    }

val Set<ChildIntrinsicDetails>.at
    get() = map {
        it.at
    }.min()
