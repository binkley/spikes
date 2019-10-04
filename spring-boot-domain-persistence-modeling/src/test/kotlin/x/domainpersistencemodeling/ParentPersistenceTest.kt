package x.domainpersistencemodeling

import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@DataJdbcTest
class ParentPersistenceTest {
    @Test
    fun shouldRoundTrip() {
    }
}
