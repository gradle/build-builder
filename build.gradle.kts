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
    implementation(libs.picocli)
    implementation(libs.jgit)
    implementation(libs.jspecify)
    implementation(libs.kotlin.stdlib)

    runtimeOnly(libs.slf4j.simple)
}

application {
    mainClass.set("org.gradle.builds.Main")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useSpock(libs.versions.spock)
            dependencies {
                implementation(gradleTestKit())
            }
            targets {
                all {
                    testTask.configure {
                        maxParallelForks = Runtime.getRuntime().availableProcessors()
                        maxHeapSize = "1g"
                        setForkEvery(50)
                    }
                }
            }
        }
    }
}
