package x.loggy.data;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.Optional;

@Repository
public interface SallyRepository
        extends CrudRepository<SallyRecord, Long> {
    String METRIC_NAME = "sally.repository";

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    <S extends SallyRecord> S save(@Nonnull S entity);

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    <S extends SallyRecord> Iterable<S> saveAll(
            @Nonnull Iterable<S> entities);

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    Optional<SallyRecord> findById(@Nonnull Long aLong);

    @Override
    @Timed(METRIC_NAME)
    boolean existsById(@Nonnull Long aLong);

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    Iterable<SallyRecord> findAll();

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    Iterable<SallyRecord> findAllById(@Nonnull Iterable<Long> longs);

    @Override
    @Timed(METRIC_NAME)
    long count();

    @Override
    @Timed(METRIC_NAME)
    void deleteById(@Nonnull Long aLong);

    @Override
    @Timed(METRIC_NAME)
    void delete(@Nonnull SallyRecord entity);

    @Override
    @Timed(METRIC_NAME)
    void deleteAll(@Nonnull Iterable<? extends SallyRecord> entities);

    @Override
    @Timed(METRIC_NAME)
    void deleteAll();
}
