plugins {
    id("java")
    id("groovy")
    id("application")
    alias(libs.plugins.kotlin.jvm)
    id("build-builder.generator-versions")
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
                        maxParallelForks = 16
                        maxHeapSize = "1g"
                        setForkEvery(50)
                    }
                }
            }
        }
    }
}
