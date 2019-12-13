import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL

val gradleWrapperVersion: String by project

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val linuxX64Main by creating {
            dependsOn(commonMain)
        }

        @Suppress("UNUSED_VARIABLE")
        val macosX64Main by creating {
            dependsOn(commonMain)
        }
    }

    val linuxX64 = linuxX64()
    val macosX64 = macosX64()

    configure(listOf(linuxX64, macosX64)) {
        binaries {
            executable {
                baseName = "hello-from-kotlin-native"
            }
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = gradleWrapperVersion
    distributionType = ALL
}
