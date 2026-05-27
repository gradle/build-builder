plugins {
    id("java")
    id("groovy")
    id("application")
    kotlin("jvm") version "2.3.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.airlift:airline:0.9")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.6.0.202603022253-r")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    runtimeOnly("org.slf4j:slf4j-simple:2.0.18")

    testImplementation(gradleTestKit())
    testImplementation("org.spockframework:spock-core:2.3-groovy-3.0")
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("org.gradle.builds.Main")
}

tasks {
    "test"(Test::class) {
        maxParallelForks = 2
    }
}
