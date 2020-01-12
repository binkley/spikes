rootProject.name = "micronaut-kitchen-sink"

pluginManagement {
    val gitPropertiesPluginVersion: String by settings
    val kotlinVersion: String by settings
    val shadowPluginVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
        id("com.gorylenko.gradle-git-properties") version gitPropertiesPluginVersion
        id("com.github.johnrengelman.shadow") version shadowPluginVersion
    }
}
