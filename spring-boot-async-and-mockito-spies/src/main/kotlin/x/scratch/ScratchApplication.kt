package x.scratch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ScratchApplication

fun main(args: Array<String>) {
    runApplication<ScratchApplication>(*args)
}
