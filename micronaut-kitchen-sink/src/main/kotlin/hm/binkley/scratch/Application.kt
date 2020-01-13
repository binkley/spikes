package hm.binkley.scratch

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import lombok.Generated
import java.time.ZoneOffset.UTC
import java.util.TimeZone

@Generated
@OpenAPIDefinition(
    // TODO: Annotate package, not class
    info = Info(
        contact = Contact(
            name = "B. K. Oxley (binkley)",
            email = "binkley@alumni.rice.edu",
            url = "https://github.com/binkley"
        ),
        description = "Experiments in Micronaut features",
        license = License(
            name = "Public Domain",
            url = "https://unlicense.org/"
        ),
        title = "Micronaut Kitchen Sink",
        version = "0.1"
    )
)
object Application {
    @JvmStatic
    fun main(args: Array<String>) { // TODO: top-level function
        System.setProperty("user.timezone", "UTC")
        TimeZone.setDefault(TimeZone.getTimeZone(UTC))

        Micronaut.build() // TODO: Simplify
            .packages("hm.binkley.scratch")
            .mainClass(Application.javaClass)
            .start()
    }
}
