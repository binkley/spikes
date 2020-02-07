rootProject.name = "micronaut-kitchen-sink"

pluginManagement {
    val detektPluginVersion: String by settings
    val gitPropertiesPluginVersion: String by settings
    val kotlinVersion: String by settings
    val ktlintGradlePlugin: String by settings
    val shadowPluginVersion: String by settings
    val testRetryPluginVersion: String by settings
    val versionsPluginVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("io.gitlab.arturbosch.detekt") version detektPluginVersion
        id("com.gorylenko.gradle-git-properties") version gitPropertiesPluginVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintGradlePlugin
        id("com.github.johnrengelman.shadow") version shadowPluginVersion
        id("org.gradle.test-retry") version testRetryPluginVersion
        id("com.github.ben-manes.versions") version versionsPluginVersion
    }
}
