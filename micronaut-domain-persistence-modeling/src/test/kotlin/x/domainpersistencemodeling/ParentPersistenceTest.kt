package x.domainpersistencemodeling

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
    }
}
