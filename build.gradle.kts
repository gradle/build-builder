plugins {
    id("java")
    id("groovy")
    id("application")
    id("org.jetbrains.kotlin.jvm").version("1.2.50")
}

repositories {
    jcenter()
    maven { url = uri("https://repo.gradle.org/gradle/repo") }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.codehaus.groovy" && requested.name == "groovy-all") {
            useTarget("org.codehaus.groovy:groovy:2.4.10")
        }
    }
}

dependencies {
    compile("io.airlift:airline:0.7")
//    compile("org.gradle:gradle-tooling-api:3.3")
    compile("org.eclipse.jgit:org.eclipse.jgit:4.9.1.201712030800-r")
    compile("org.jetbrains.kotlin:kotlin-stdlib:1.2.50")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.2.50")

    runtime("org.slf4j:slf4j-simple:1.7.25")

    testCompile(gradleTestKit())
    testCompile("org.codehaus.groovy:groovy:2.4.10")
    testCompile("org.spockframework:spock-core:1.0-groovy-2.4")
    testRuntime("cglib:cglib-nodep:2.2.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
application {
    mainClassName = "org.gradle.builds.Main"
}

tasks {
    "test"(Test::class) {
        maxParallelForks = 2
    }
}