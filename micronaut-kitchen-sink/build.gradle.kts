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

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.allopen")
    id("com.gorylenko.gradle-git-properties")
    id("com.github.johnrengelman.shadow")
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
    implementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.configuration:micronaut-micrometer-core")
    implementation("io.micronaut.configuration:micronaut-micrometer-registry-prometheus")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-tracing")
    implementation("javax.annotation:javax.annotation-api")
    kapt(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-inject-java")
    kapt("io.micronaut:micronaut-validation")
    kapt("io.micronaut.configuration:micronaut-openapi")
    kaptTest(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kaptTest("io.micronaut:micronaut-inject-java")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    runtimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    runtimeOnly("io.jaegertracing:jaeger-thrift")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    testImplementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    testImplementation("io.micronaut.test:micronaut-test-kotlintest")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:$kotlinTestVersion")
}

allOpen {
    annotation("io.micronaut.aop.Around")
}

application {
    mainClassName = "hm.binkley.scratch.Application"
}

gitProperties {
    dateFormat = "yyyy-MM-dd'T'HH:mm:ssX"
    dateFormatTimeZone = "UTC"
}

jacoco {
    toolVersion = jacocoVersion
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
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
                    minimum = BigDecimal.ONE
                }
            }
        }
    }

    check {
        dependsOn(jacocoTestCoverageVerification)
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
