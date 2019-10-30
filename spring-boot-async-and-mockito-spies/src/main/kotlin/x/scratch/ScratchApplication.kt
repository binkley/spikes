package x.scratch

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@SpringBootApplication
class ScratchApplication

fun main(args: Array<String>) {
    runApplication<ScratchApplication>(*args)
}

@Component
class Sally @Autowired constructor(
        private val bob: Bob) {
    fun runItEventuallyButQuickly() {
        bob.runItEventuallyButQuickly()
    }

    fun runItEventuallyButSlowly() {
        bob.runItEventuallyButSlowly()
    }
}

@Component
open class Bob {
    @Async
    open fun runItEventuallyButQuickly() {
    }

    @Async(SLOW_EXECUTOR_BEAN_NAME)
    open fun runItEventuallyButSlowly() {
    }
}
