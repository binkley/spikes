package x.domainpersistencemodeling

import io.micronaut.runtime.Micronaut

object DemoApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("x")
                .mainClass(DemoApplication.javaClass)
                .start()
    }
}
