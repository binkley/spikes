package x.micronaut

import io.micronaut.runtime.Micronaut
import org.jetbrains.exposed.sql.Database
import javax.annotation.PostConstruct
import javax.sql.DataSource

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("kotlin.micronaut")
                .mainClass(Application.javaClass)
                .start()
    }

    @PostConstruct
    fun init(dataSource: DataSource) {
        Database.connect(dataSource)
    }
}
