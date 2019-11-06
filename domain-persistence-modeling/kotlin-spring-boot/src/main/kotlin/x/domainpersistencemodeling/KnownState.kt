package x.domainpersistencemodeling

enum class KnownState(val relevant: Boolean) {
    ENABLED(true), DISABLED(false);

    companion object {
        fun forName(name: String) = try {
            valueOf(name)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
