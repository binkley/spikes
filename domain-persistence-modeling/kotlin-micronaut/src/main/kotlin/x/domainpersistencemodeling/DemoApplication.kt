package x.domainpersistencemodeling

import io.micronaut.runtime.Micronaut
import lombok.Generated
import java.time.ZoneOffset.UTC
import java.util.TimeZone

@Generated
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
