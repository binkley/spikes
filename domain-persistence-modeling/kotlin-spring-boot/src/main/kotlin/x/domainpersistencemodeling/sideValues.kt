package x.domainpersistencemodeling

fun Parent.currentSideValues() =
        if (sideValues.isNotEmpty()) sideValues
        else children.map {
            it.currentSideValues()
        }.filter {
            !it.isEmpty()
        }.flatten().toSortedSet()

fun Child.currentSideValues() =
        if (sideValues.isNotEmpty()) sideValues
        else defaultSideValues
