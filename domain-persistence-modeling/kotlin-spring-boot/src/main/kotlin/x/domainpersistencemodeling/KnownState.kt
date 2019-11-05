package x.domainpersistencemodeling

enum class KnownState {
    ENABLED, DISABLED;

    companion object {
        fun forName(name: String): KnownState? {
            try {
                return valueOf(name)
            } catch (e: IllegalArgumentException) {
                return null;
            }
        }
    }
}
