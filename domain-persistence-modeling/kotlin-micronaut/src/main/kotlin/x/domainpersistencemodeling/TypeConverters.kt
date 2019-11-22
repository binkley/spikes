package x.domainpersistencemodeling

import io.micronaut.context.annotation.Factory
import io.micronaut.core.convert.TypeConverter
import java.util.Optional
import javax.inject.Singleton

@Factory
class TypeConverters {
    @Singleton
    fun <T> sqlArrayToSet() =
            TypeConverter<java.sql.Array, Set<T>> { sqlArray, targetType, context ->
                Optional.of((sqlArray.array as Array<T>).toSet())
            }
}
