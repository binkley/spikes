package x.scratch

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Bean
import org.springframework.core.task.TaskExecutor
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicBoolean

@SpringBootTest
class ScratchApplicationTests {
    companion object {
        private val executorRan = AtomicBoolean(false)
        private val taskRan = AtomicBoolean(false)
    }

    @SpyBean
    private lateinit var bob: Bob
    @Autowired
    private lateinit var sally: Sally

    @Test
    fun shouldWaitOnBob() {
        assertThat(mockingDetails(bob).isSpy).`as`("Should be a spy")
                .isTrue()

        sally.runIt()

        try {
            verify(bob, timeout(SECONDS.toMillis(2L))).runItEventually()
        } catch (e: AssertionError) {
            if (!executorRan.get())
                throw AssertionError("Did not even run the executor")
            if (!taskRan.get())
                throw AssertionError("Did not even run the task")
            throw e
        }
    }

    @TestConfiguration
    class MyTestConfiguration {
        @Bean
        fun slowExecutor() = TaskExecutor { task ->
            executorRan.set(true)
            Thread {
                SECONDS.sleep(1L)
                taskRan.set(true)
                task.run()
            }.start()
        }
    }
}
