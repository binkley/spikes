plugins {
    kotlin("multiplatform") version "1.3.61"
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
    gradleVersion = "6.0.1"
    distributionType = Wrapper.DistributionType.ALL
}
