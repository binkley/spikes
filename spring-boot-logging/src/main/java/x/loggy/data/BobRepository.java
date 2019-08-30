package x.loggy.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BobRepository
        extends CrudRepository<BobRecord, Long> {
}
