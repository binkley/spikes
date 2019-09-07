package x.loggy.data;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BobRepository
        extends CrudRepository<BobRecord, Long> {
    @Override
    @Timed("bob.repository")
    <S extends BobRecord> S save(S entity);

    @Override
    @Timed("bob.repository")
    <S extends BobRecord> Iterable<S> saveAll(Iterable<S> entities);

    @Override
    @Timed("bob.repository")
    Optional<BobRecord> findById(Long aLong);

    @Override
    @Timed("bob.repository")
    boolean existsById(Long aLong);

    @Override
    @Timed("bob.repository")
    Iterable<BobRecord> findAll();

    @Override
    @Timed("bob.repository")
    Iterable<BobRecord> findAllById(Iterable<Long> longs);

    @Override
    @Timed("bob.repository")
    long count();

    @Override
    @Timed("bob.repository")
    void deleteById(Long aLong);

    @Override
    @Timed("bob.repository")
    void delete(BobRecord entity);

    @Override
    @Timed("bob.repository")
    void deleteAll(Iterable<? extends BobRecord> entities);

    @Override
    @Timed("bob.repository")
    void deleteAll();
}
