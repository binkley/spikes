package hm.binkley.scratch

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import java.time.ZoneOffset.UTC
import java.util.TimeZone

@OpenAPIDefinition(
    info = Info(
        title = "micronaut-kitchen-sink",
        version = "0.0"
    )
)
object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        System.setProperty("user.timezone", "UTC")
        TimeZone.setDefault(TimeZone.getTimeZone(UTC))
        
        Micronaut.build()
            .packages("hm.binkley.scratch")
            .mainClass(Application.javaClass)
            .start()
    }
}
