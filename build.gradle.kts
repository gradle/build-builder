plugins {
    id("java")
    id("groovy")
    id("application")
    kotlin("jvm") version "2.1.20"
}

repositories {
    mavenCentral()
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.codehaus.groovy" && requested.name == "groovy-all") {
            useTarget("org.codehaus.groovy:groovy:2.4.10")
        }
    }
}

dependencies {
    implementation("io.airlift:airline:0.7")
    implementation("org.eclipse.jgit:org.eclipse.jgit:4.9.1.201712030800-r")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    runtimeOnly("org.slf4j:slf4j-simple:1.7.25")

    testImplementation(gradleTestKit())
    testImplementation("org.codehaus.groovy:groovy:2.4.10")
    testImplementation("org.spockframework:spock-core:1.0-groovy-2.4")
    testRuntimeOnly("cglib:cglib-nodep:2.2.2")
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
