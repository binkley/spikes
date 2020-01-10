package micronaut.kitchen.sink

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = Info(
        title = "micronaut-kitchen-sink",
        version = "0.0"
    )
)
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("micronaut.kitchen.sink")
            .mainClass(Application.javaClass)
            .start()
    }
}
