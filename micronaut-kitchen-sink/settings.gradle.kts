rootProject.name = "micronaut-kitchen-sink"

pluginManagement {
    val detektPluginVersion: String by settings
    val gitPropertiesPluginVersion: String by settings
    val kotlinVersion: String by settings
    val ktlintGradlePlugin: String by settings
    val shadowPluginVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
        id("io.gitlab.arturbosch.detekt") version detektPluginVersion
        id("com.gorylenko.gradle-git-properties") version gitPropertiesPluginVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintGradlePlugin
        id("com.github.johnrengelman.shadow") version shadowPluginVersion
    }
}
