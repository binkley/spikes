rootProject.name = "micronaut-kitchen-sink"

pluginManagement {
    val kotlinVersion: String by settings
    val shadowPluginVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
        id("com.github.johnrengelman.shadow") version shadowPluginVersion
    }
}
