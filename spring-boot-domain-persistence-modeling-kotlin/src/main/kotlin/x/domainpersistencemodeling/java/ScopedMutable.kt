package x.domainpersistencemodeling.java

import java.util.function.Consumer

interface ScopedMutable<Domain, Mutable> {
    fun update(block: Consumer<Mutable>): Domain
}
