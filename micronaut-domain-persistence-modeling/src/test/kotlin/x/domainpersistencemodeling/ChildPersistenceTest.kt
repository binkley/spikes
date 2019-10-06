package x.domainpersistencemodeling

import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class ChildPersistenceTest {
    @Inject
    lateinit var children: ChildFactory
    @Inject
    lateinit var parents: ParentFactory
    @Inject
    lateinit var testListener: TestListener<ChildChangedEvent>

    @Test
    fun shouldRoundTrip() {
    }
}
