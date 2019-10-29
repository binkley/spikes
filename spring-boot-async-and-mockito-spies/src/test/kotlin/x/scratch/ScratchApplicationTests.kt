package x.scratch

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ScratchApplicationTests @Autowired constructor(
        private val sally: Sally) {
    @Test
    fun shouldWaitOnBob() {
        sally.runIt()
    }
}
