package x.domainpersistencemodeling

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class ParentPersistenceTest {
    @Inject
    lateinit var parents: ParentFactory
    @Inject
    lateinit var testListener: TestListener<ParentChangedEvent>

    @Test
    fun shouldRoundTrip() {
        val unsaved = parents.findExistingOrCreateNew("a")
        val saved = unsaved.update {
            save()
        }

        val found = parents.findExisting(unsaved.naturalId)

        expect(found).toBe(saved)
    }
}
