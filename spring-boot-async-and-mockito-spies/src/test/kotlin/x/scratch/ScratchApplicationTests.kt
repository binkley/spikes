package x.scratch

import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean

@SpringBootTest
class ScratchApplicationTests {
    @SpyBean
    private lateinit var bob: Bob
    @Autowired
    private lateinit var sally: Sally

    @Test
    fun shouldWaitOnBob() {
        sally.runIt()

        verify(bob).runItEventually()
    }
}
