package x.txns;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FooUpdater {
    private final FooRepository foos;

    @Transactional
    public boolean updateFoo(final int value) {
        final var found = foos.findByValueWithLock(value);
        if (found.isEmpty())
            return false;
        final var foo = found.get();
        foo.setValue(foo.getValue() + 1);
        foos.save(foo);
        return true;
    }
}
