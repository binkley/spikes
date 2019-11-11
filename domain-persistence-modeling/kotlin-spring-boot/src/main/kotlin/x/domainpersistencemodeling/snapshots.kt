package x.domainpersistencemodeling

import x.domainpersistencemodeling.ParentRepository.ParentRecord

internal fun ParentRecord.toSnapshot(computed: ParentComputedDetails) =
        ParentSnapshot(naturalId, otherNaturalId, state, computed.at, value,
                sideValues, version)
