package x.txns;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.stream.Stream;

public interface FooRepository
        extends CrudRepository<FooRecord, Long> {
    @Query("SELECT * FROM FOO")
    Stream<FooRecord> readAll();
}
