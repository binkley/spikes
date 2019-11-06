package x.domainpersistencemodeling

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
// TODO: Why doesn't Spring plugin for Kotlin make this `open`?
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
