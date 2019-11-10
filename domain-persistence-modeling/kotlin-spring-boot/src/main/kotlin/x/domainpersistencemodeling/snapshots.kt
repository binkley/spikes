package x.domainpersistencemodeling

import x.domainpersistencemodeling.ChildRepository.ChildRecord
import x.domainpersistencemodeling.ParentRepository.ParentRecord

internal fun ParentRecord.toSnapshot(computed: ParentComputedDetails) =
        ParentSnapshot(naturalId, otherNaturalId, state, computed.at, value,
                sideValues, version)

internal fun ChildRecord.toSnapshot(computed: ChildComputedDetails) =
        ChildSnapshot(naturalId, otherNaturalId, parentNaturalId, state, at,
                value, sideValues, version)
