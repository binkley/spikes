package x.domainpersistencemodeling

import x.domainpersistencemodeling.child.ChildSimpleDetails
import x.domainpersistencemodeling.parent.ParentDependentDetails
import x.domainpersistencemodeling.parent.ParentSimpleDetails
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal val atZero = Instant.EPOCH.atOffset(ZoneOffset.UTC)

internal fun computeDue(
    parent: ParentSimpleDetails,
    children: Set<ChildSimpleDetails>
): OffsetDateTime? {
    val at = parent.at
    val childrenAt = children.at
    return when {
        null == at -> childrenAt
        at <= childrenAt -> at
        else -> throw DomainException("Parent \"at\" after children \"at\"")
    }
}

internal val Set<ChildSimpleDetails>.at
    get() = map {
        it.at
    }.min()

internal val <T> T.currentSideValues: Set<String>
        where T : ParentSimpleDetails,
              T : ParentDependentDetails
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

internal val ChildSimpleDetails.currentSideValues: Set<String>
    get() = when {
        !relevant -> setOf()
        sideValues.isNotEmpty() -> sideValues
        else -> defaultSideValues
    }

private val ChildSimpleDetails.relevant: Boolean
    get() = KnownState.forName(this.state)?.relevant ?: true
