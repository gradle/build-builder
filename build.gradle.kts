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

java {
    sourceCompatibility = JavaVersion.VERSION_11
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
                        // Subprocess output is now streamed to per-build files
                        // in AbstractIntegrationTest.start / buildSucceeds, so
                        // the test JVM no longer holds the Swift/Cpp compile
                        // output in memory. 15 of 16 forks in the prior run
                        // capped at ~50 MB; this drops the per-fork heap from
                        // 4 GB to 1 GB and restores 16-way parallelism.
                        maxParallelForks = 16
                        maxHeapSize = "1g"
                        setForkEvery(50)
                        jvmArgs(
                            "-Xlog:gc*,gc+heap=info:file=${layout.buildDirectory.get().asFile}/test-results/gc-%p.log:time,level,tags",
                            "-XX:+HeapDumpOnOutOfMemoryError",
                            "-XX:HeapDumpPath=${layout.buildDirectory.get().asFile}/test-results"
                        )
                    }
                }
            }
        }
    }
}
