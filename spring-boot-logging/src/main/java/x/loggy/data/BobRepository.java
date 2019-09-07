package x.loggy.data;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.Optional;

@Repository
public interface BobRepository
        extends CrudRepository<BobRecord, Long> {
    String METRIC_NAME = "bob.repository";

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    <S extends BobRecord> S save(@Nonnull S entity);

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    <S extends BobRecord> Iterable<S> saveAll(@Nonnull Iterable<S> entities);

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    Optional<BobRecord> findById(@Nonnull Long aLong);

    @Override
    @Timed(METRIC_NAME)
    boolean existsById(@Nonnull Long aLong);

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    Iterable<BobRecord> findAll();

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    Iterable<BobRecord> findAllById(@Nonnull Iterable<Long> longs);

    @Override
    @Timed(METRIC_NAME)
    long count();

    @Override
    @Timed(METRIC_NAME)
    void deleteById(@Nonnull Long aLong);

    @Override
    @Timed(METRIC_NAME)
    void delete(@Nonnull BobRecord entity);

    @Override
    @Timed(METRIC_NAME)
    void deleteAll(@Nonnull Iterable<? extends BobRecord> entities);

    @Override
    @Timed(METRIC_NAME)
    void deleteAll();
}
