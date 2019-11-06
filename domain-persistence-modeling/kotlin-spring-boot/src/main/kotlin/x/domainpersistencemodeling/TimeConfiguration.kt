package x.domainpersistencemodeling

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.convert.converter.Converter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@Configuration class TimeConfiguration {
    @Bean
    fun offsetDateTimeReadConverter() =
            Converter<Timestamp, OffsetDateTime> {
                it.toInstant().atOffset(UTC)
            }

    @Bean
    fun offsetDateTimeWriteConverter() =
            Converter<OffsetDateTime, Timestamp> {
                Timestamp.from(it.toInstant())
            }

    @Bean
    @Primary
    fun timeJdbcCustomConversions(
            offsetDateTimeReadConverter: Converter<Timestamp, OffsetDateTime>,
            offsetDateTimeWriteConverter: Converter<OffsetDateTime, Timestamp>) =
            JdbcCustomConversions(listOf(
                    offsetDateTimeReadConverter,
                    offsetDateTimeWriteConverter))
}
