package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.context.annotation.Import

@AutoConfigureTestDatabase(replace = NONE)
@DataJdbcTest
@Import(value = [PersistedParentFactory::class, TestListener::class])
class ChildPersistenceTest {
    @Autowired
    lateinit var children: ParentFactory
    @Autowired
    lateinit var testListener: TestListener<ParentChangedEvent>

    @Test
    fun shouldRoundTrip() {
        val unsaved = children.findExistingOrCreateNew("a")
        val saved = unsaved.update {
            save()
        }!!

        val found = children.findExisting(unsaved.naturalId)!!

        expect(found).toBe(saved)
    }

    @Test
    fun shouldBeUnsuableAfterDelete() {
        val unsaved = children.findExistingOrCreateNew("a")
        val saved = unsaved.update {
            save()
            delete()
        }

        expect(saved).toBe(null)
    }
}
