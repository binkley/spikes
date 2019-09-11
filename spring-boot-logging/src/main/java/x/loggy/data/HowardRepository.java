package x.loggy.data;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.Optional;

@Repository
public interface HowardRepository
        extends CrudRepository<HowardRecord, Long> {
    String METRIC_NAME = "howard.repository";

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    <S extends HowardRecord> S save(@Nonnull S entity);

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    <S extends HowardRecord> Iterable<S> saveAll(
            @Nonnull Iterable<S> entities);

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    Optional<HowardRecord> findById(@Nonnull Long aLong);

    @Override
    @Timed(METRIC_NAME)
    boolean existsById(@Nonnull Long aLong);

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    Iterable<HowardRecord> findAll();

    @Nonnull
    @Override
    @Timed(METRIC_NAME)
    Iterable<HowardRecord> findAllById(@Nonnull Iterable<Long> longs);

    @Override
    @Timed(METRIC_NAME)
    long count();

    @Override
    @Timed(METRIC_NAME)
    void deleteById(@Nonnull Long aLong);

    @Override
    @Timed(METRIC_NAME)
    void delete(@Nonnull HowardRecord entity);

    @Override
    @Timed(METRIC_NAME)
    void deleteAll(@Nonnull Iterable<? extends HowardRecord> entities);

    @Override
    @Timed(METRIC_NAME)
    void deleteAll();
}
