plugins {
    id("java")
    id("groovy")
    id("application")
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.airline)
    implementation(libs.jgit)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.stdlib.jdk8)

    runtimeOnly(libs.slf4j.simple)

    testImplementation(gradleTestKit())
    testImplementation(libs.spock.core)
    testImplementation(libs.junit)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("org.gradle.builds.Main")
}

tasks.named<Test>("test") {
    maxParallelForks = 2
}
