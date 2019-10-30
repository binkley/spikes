package x.scratch

import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.task.TaskExecutor
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

@TestConfiguration
class MyTestConfiguration {
    @Bean
    @Primary
    fun testingExecutor() = TestingExecutor()
}

class TestingExecutor
    : Executor,
        TaskExecutor {
    val tasksRan = ArrayBlockingQueue<Runnable>(10)

    override fun execute(task: Runnable) {
        Thread {
            task.run()
            assertThat(tasksRan.offer(task))
                    .`as`("Too many tasks ran without checking")
                    .isTrue()
        }.start()
    }

    fun awaitTaskRan(timeout: Long, unit: TimeUnit) =
            assertThat(tasksRan.poll(timeout, unit))
                    .`as`("No task ran in time")
                    .isNotNull
}
