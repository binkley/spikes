plugins {
    kotlin("multiplatform") version "1.3.61"
}

repositories {
    mavenCentral()
}

kotlin {
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
