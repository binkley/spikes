package x.txns;

import org.springframework.data.repository.CrudRepository;

public interface FooRepository
        extends CrudRepository<Foo, String> {
}
