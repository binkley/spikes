package x.loggy.data;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.Optional;

@Repository
public interface BobRepository
        extends CrudRepository<BobRecord, Long> {
    @Nonnull
    @Override
    @Timed("bob.repository")
    <S extends BobRecord> S save(@Nonnull S entity);

    @Nonnull
    @Override
    @Timed("bob.repository")
    <S extends BobRecord> Iterable<S> saveAll(@Nonnull Iterable<S> entities);

    @Nonnull
    @Override
    @Timed("bob.repository")
    Optional<BobRecord> findById(@Nonnull Long aLong);

    @Override
    @Timed("bob.repository")
    boolean existsById(@Nonnull Long aLong);

    @Nonnull
    @Override
    @Timed("bob.repository")
    Iterable<BobRecord> findAll();

    @Nonnull
    @Override
    @Timed("bob.repository")
    Iterable<BobRecord> findAllById(@Nonnull Iterable<Long> longs);

    @Override
    @Timed("bob.repository")
    long count();

    @Override
    @Timed("bob.repository")
    void deleteById(@Nonnull Long aLong);

    @Override
    @Timed("bob.repository")
    void delete(@Nonnull BobRecord entity);

    @Override
    @Timed("bob.repository")
    void deleteAll(@Nonnull Iterable<? extends BobRecord> entities);

    @Override
    @Timed("bob.repository")
    void deleteAll();
}
