package x.txns;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.NESTED;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ANestedTransaction {
    private final Logger logger;

    @Transactional(propagation = NESTED)
    public void undoWrongSave() {
        logger.warn("FAILING NESTED TRANSACTION");
        throw new NullPointerException("NOT REALLY NULL");
    }
}
