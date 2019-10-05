package x.domainpersistencemodeling

import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE

@AutoConfigureTestDatabase(replace = NONE)
@DataJdbcTest
class ParentPersistenceTest {
    @Test
    fun shouldRoundTrip() {
    }
}
