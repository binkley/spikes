package x.scratch

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component

@EnableAsync
@SpringBootApplication
class ScratchApplication

fun main(args: Array<String>) {
    runApplication<ScratchApplication>(*args)
}

@Component
class Sally @Autowired constructor(
        private val bob: Bob) {
    fun runIt() {
        bob.runItEventually()
    }
}

@Component
open class Bob {
    @Async
    open fun runItEventually() {
    }
}
