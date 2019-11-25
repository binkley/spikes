package x.domainpersistencemodeling

import io.micronaut.runtime.Micronaut
import java.time.ZoneOffset.UTC
import java.util.TimeZone

object DemoApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        TimeZone.setDefault(TimeZone.getTimeZone(UTC))

        Micronaut.build()
            .packages("x")
            .mainClass(DemoApplication.javaClass)
            .start()
    }
}
