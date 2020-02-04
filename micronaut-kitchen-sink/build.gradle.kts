import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val gradleWrapperVersion: String by project
val jacksonVersion: String by project
val jacocoVersion: String by project
val kotlinVersion: String by project
val kotlinTestVersion: String by project
val logbackVersion: String by project
val logstashVersion: String by project
val micronautVersion: String by project
val mockkVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("io.gitlab.arturbosch.detekt")
    id("com.gorylenko.gradle-git-properties")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.johnrengelman.shadow")
    id("org.gradle.test-retry")
    application
    jacoco
}

version = "0.1"
group = "hm.binkley.scratch"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    kapt(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut.configuration:micronaut-openapi")
    kapt("io.micronaut:micronaut-inject-java")
    kapt("io.micronaut:micronaut-validation")

    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.micronaut.configuration:micronaut-micrometer-core")
    implementation("io.micronaut.configuration:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-tracing")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("javax.annotation:javax.annotation-api")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")

    kaptTest(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kaptTest("io.micronaut:micronaut-inject-java")

    testImplementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    testImplementation("io.kotlintest:kotlintest-runner-junit5:$kotlinTestVersion")
    testImplementation("io.micronaut.test:micronaut-test-kotlintest")
    testImplementation("io.mockk:mockk:$mockkVersion")

    runtimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    runtimeOnly("io.jaegertracing:jaeger-thrift")
}

allOpen {
    annotation("io.micronaut.aop.Around")
    annotation("io.micronaut.http.annotation.Controller")
}

application {
    mainClassName = "hm.binkley.scratch.Application"
}

detekt {
    failFast = true
    // No support yet for configuring direcly in Gradle
    config = files("config/detekt.yml")
}

gitProperties {
    dateFormat = "yyyy-MM-dd'T'HH:mm:ssX"
    dateFormatTimeZone = "UTC"
}

jacoco {
    toolVersion = jacocoVersion
}

ktlint {
    outputColorName.set("RED")
}

// test {
//    retry {
//        failOnPassedAfterRetry = true
//        maxFailures = 42
//        maxRetries = 1
//    }
// }

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            javaParameters = true
        }
    }

    test {
        useJUnitPlatform()

        finalizedBy(jacocoTestReport)
    }

    jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = BigDecimal.ZERO // TODO: Real coverage
                }
            }
        }
    }

    // TODO: How to move this out to configuration, like Groovy example
    withType<Test> {
        // See https://blog.gradle.org/gradle-flaky-test-retry-plugin
        retry {
            failOnPassedAfterRetry.set(true)
            maxRetries.set(6)
            maxFailures.set(1)
        }
    }

    check {
        dependsOn(jacocoTestCoverageVerification)
        // TODO: Do not run both ktlintCheck and ktlintFormat
        dependsOn(ktlintFormat)
    }

    shadowJar {
        mergeServiceFiles()
    }

    named<JavaExec>("run") {
        jvmArgs(
            "-noverify",
            "-XX:TieredStopAtLevel=1",
            "-Dcom.sun.management.jmxremote"
        )
    }

    withType<Wrapper> {
        gradleVersion = gradleWrapperVersion
        distributionType = ALL
    }
}
