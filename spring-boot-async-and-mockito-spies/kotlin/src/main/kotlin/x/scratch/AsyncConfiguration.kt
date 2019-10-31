package x.scratch

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit.MINUTES

const val SLOW_EXECUTOR_BEAN_NAME = "slowExecutor"

@Configuration
@EnableAsync
open class AsyncConfiguration {
    @Bean
    @Primary
    fun executor() = Executor { command ->
        command.run()
    }

    @Bean
    @ConditionalOnMissingBean(name = [SLOW_EXECUTOR_BEAN_NAME])
    fun slowExecutor() = Executor { command ->
        MINUTES.sleep(1L)
        command.run()
    }
}

