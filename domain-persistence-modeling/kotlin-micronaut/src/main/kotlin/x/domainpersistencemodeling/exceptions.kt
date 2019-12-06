package x.domainpersistencemodeling

import lombok.Generated

class DomainException(message: String) : RuntimeException(message)

@Generated // Lie to JaCoCo -- this should hopefully never execute :)
class Bug(message: String) : IllegalStateException("BUG: $message")
