package x.txns;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.NESTED;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FailedNestedTransaction {
    private final FooRepository foos;
    private final Logger logger;

    @Transactional(propagation = NESTED)
    public void failedSave(final FooRecord saved) {
        saved.id = null;
        logger.info("FAILING NESTED TRANSACTION: {}", saved);
        foos.save(saved); // No duplicates
    }
}
