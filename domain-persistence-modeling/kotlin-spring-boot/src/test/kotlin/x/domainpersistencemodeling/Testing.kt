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

    internal fun createNewParent() =
            parents.createNew(parentNaturalId)

    internal fun findExistingOrCreateNewParent() =
            parents.findExistingOrCreateNew(parentNaturalId)

    internal fun newSavedParent(): Parent {
        val saved = createNewParent().save()
        expect(saved.changed).toBe(true)
        val parent = saved.domain
        testListener.reset()
        return parent
    }

    internal fun currentPersistedParent(): Parent {
        return parents.findExisting(parentNaturalId)!!
    }

    internal fun allChildren() = children.all()

    internal fun newSavedUnassignedChild(): UnassignedChild {
        val saved = createNewUnassignedChild().save()
        expect(saved.changed).toBe(true)
        val child = saved.domain
        testListener.reset()
        return child as UnassignedChild
    }

    internal fun currentPersistedChild(): Child {
        return children.findExisting(childNaturalId)!!
    }

    internal fun createNewUnassignedChild() =
            children.createNewUnassigned(childNaturalId)

    internal fun findExistingOrCreateNewUnassignedChild() =
            children.findExistingOrCreateNewUnassigned(childNaturalId)
}
