package x.scratch

import org.junit.jupiter.api.Test
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Bean
import org.springframework.core.task.TaskExecutor

@SpringBootTest
class ScratchApplicationTests {
    companion object {
        var executorRan: Boolean = false
        var taskRan: Boolean = false
    }

    @SpyBean
    private lateinit var bob: Bob
    @Autowired
    private lateinit var sally: Sally

    @Test
    fun shouldWaitOnBob() {
        sally.runIt()

        try {
            verify(bob, timeout(2_000L)).runItEventually()
        } catch (e: AssertionError) {
            if (!executorRan)
                throw AssertionError("Did not even run the executor")
            if (!taskRan)
                throw AssertionError("Did not even run the task")
            throw e
        }
    }

    @TestConfiguration
    class MyTestConfiguration {
        @Bean
        fun slowExecutor() = TaskExecutor { task ->
            executorRan = true
            Thread {
                Thread.sleep(1_000L)
                task.run()
                taskRan = true
            }.run()
        }
    }
}
