package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

internal const val parentNaturalId = "a"
internal const val childNaturalId = "p"

@Component
internal open class Testing @Autowired constructor(
        val parents: ParentFactory,
        val children: ChildFactory,
        val testListener: TestListener<DomainChangedEvent<*>>) {
    internal fun expectDomainChangedEvents() = testListener.expectNext
    internal fun resetDomainChangedEvents() = testListener.reset()

    internal fun allParents() = parents.all()

    internal fun createNewParent(naturalId: String = parentNaturalId) =
            parents.createNew(naturalId)

    internal fun findExistingOrCreateNewParent(
            naturalId: String = parentNaturalId) =
            parents.findExistingOrCreateNew(naturalId)

    internal fun newSavedParent(): Parent {
        val saved = createNewParent().save()
        expect(saved.changed).toBe(true)
        val parent = saved.domain
        testListener.reset()
        return parent
    }

    internal fun currentPersistedParent(
            naturalId: String = parentNaturalId): Parent {
        return parents.findExisting(naturalId)!!
    }

    internal fun allChildren() = children.all()

    internal fun newSavedUnassignedChild(): UnassignedChild {
        val saved = createNewUnassignedChild().save()
        expect(saved.changed).toBe(true)
        val child = saved.domain
        testListener.reset()
        return child as UnassignedChild
    }

    internal fun currentPersistedChild(
            naturalId: String = childNaturalId): Child {
        return children.findExisting(naturalId)!!
    }

    internal fun createNewUnassignedChild(
            naturalId: String = childNaturalId) =
            children.createNewUnassigned(naturalId)

    internal fun findExistingOrCreateNewUnassignedChild(
            naturalId: String = childNaturalId) =
            children.findExistingOrCreateNewUnassigned(naturalId)
}
