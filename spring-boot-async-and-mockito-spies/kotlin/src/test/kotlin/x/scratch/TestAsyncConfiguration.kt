package x.scratch

import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

@EnableAsync
@TestConfiguration
class TestAsyncConfiguration {
    @Bean
    @Primary
    fun testingExecutor() = TestingExecutor("default")

    @Bean(SLOW_EXECUTOR_BEAN_NAME)
    fun alsoTestingExecutor() = TestingExecutor("special")
}

class TestingExecutor(val name: String)
    : Executor,
        TaskExecutor {
    val executorsCalled = ArrayBlockingQueue<TestingExecutor>(10)
    val tasksRan = ArrayBlockingQueue<Runnable>(10)

    override fun execute(command: Runnable) {
        assertThat(executorsCalled.offer(this))
                .`as`("Too many executors called without checking")
                .isTrue()
        Thread {
            command.run()
            assertThat(tasksRan.offer(command))
                    .`as`("Too many tasks ran without checking")
                    .isTrue()
        }.start()
    }

    fun awaitExecutorCalled(timeout: Long, unit: TimeUnit): TestingExecutor {
        val executor = executorsCalled.poll(timeout, unit)
        assertThat(executor)
                .`as`("No executor called in time")
                .isNotNull()
        return executor!!
    }

    fun awaitTaskRan(timeout: Long, unit: TimeUnit): Runnable {
        val task = tasksRan.poll(timeout, unit)
        assertThat(task)
                .`as`("No task ran in time")
                .isNotNull()
        return task!!
    }

    override fun toString(): String {
        return "$name:${super.toString()}"
    }
}
