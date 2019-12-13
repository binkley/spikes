plugins {
    kotlin("multiplatform") version "1.3.61"
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        val commonMain by getting {}

        @Suppress("UNUSED_VARIABLE")
        val linuxX64Main by creating {
            dependsOn(commonMain)
        }

        @Suppress("UNUSED_VARIABLE")
        val macosX64Main by creating {
            dependsOn(commonMain)
        }
    }

    linuxX64 {
        binaries {
            executable()
        }
    }

    macosX64 {
        binaries {
            executable()
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.0.1"
    distributionType = Wrapper.DistributionType.ALL
}
