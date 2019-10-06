package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.context.annotation.Import

@AutoConfigureTestDatabase(replace = NONE)
@DataJdbcTest
@Import(value = [
    PersistedChildFactory::class,
    PersistedParentFactory::class,
    TestListener::class])
class ChildPersistenceTest {
    @Autowired
    lateinit var children: ChildFactory
    @Autowired
    lateinit var parents: ParentFactory
    @Autowired
    lateinit var testListener: TestListener<ChildChangedEvent>

    @AfterEach
    fun tearDown() {
        children.all().forEach {
            it.delete()
        }
        parents.all().forEach {
            it.delete()
        }
    }

    @Test
    fun shouldRoundTrip() {
        val unsaved = newChild()
        val saved = unsaved.update {
            save()
        }!!

        val found = children.findExisting(saved.naturalId)!!

        expect(found).toBe(saved)
    }

    @Test
    fun shouldBeUnsuableAfterDelete() {
        val unsaved = newChild()
        val saved = unsaved.update {
            save()
            delete()
        }

        expect(saved).toBe(null)
    }

    @Test
    fun shouldAddToParent() {
        val parent = parents.findExistingOrCreateNew("a").updateAndSave { }

        val unsaved = newChild()
        val saved = unsaved.updateAndSave {
            addTo(parent.asResource())
        }

        val found = children.findExisting(saved.naturalId)!!

//        expect(saved.parentNaturalId).toBe(parent.naturalId)
    }

    private fun newChild() = children.findExistingOrCreateNew("p")
}
