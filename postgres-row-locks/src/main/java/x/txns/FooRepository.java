package x.txns;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.stream.Stream;

public interface FooRepository
        extends CrudRepository<FooRecord, Long> {
    @Query("SELECT * FROM FOO")
    Stream<FooRecord> readAll();

    @Query("SELECT * FROM FOO WHERE key = :key")
    Optional<FooRecord> findByKey(@Param("key") String key);
}
