import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

// The installed distribution is launched by consumers on whatever JVM they happen to have —
// gradle/gradle execs `bin/build-builder` from its performance-test agents. Without an explicit
// release, the class files target the build JVM (currently 25) and the launcher dies with
// UnsupportedClassVersionError anywhere older.
val distributionJvmTarget = 17

tasks.withType<JavaCompile>().configureEach {
    options.release = distributionJvmTarget
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(distributionJvmTarget.toString())
    }
}

// gradle/gradle generates its Swift performance-test projects by running `bin/build-builder`
// from the installed distribution (see `BuildBuilderGenerator` there). Nothing else in this
// build exercises that path, so breakage in it — a wrong install task name, an unrunnable
// class file version — only ever surfaces on the consumer's CI. Cover it here instead.
val smokeTestInstalledDistribution by tasks.registering(Exec::class) {
    description = "Runs the CLI from the installed distribution, the way gradle/gradle consumes it."
    group = LifecycleBasePlugin.VERIFICATION_GROUP

    dependsOn(tasks.installDist)

    val launcher = layout.buildDirectory.file("install/${project.name}/bin/${project.name}")
    val generatedDir = layout.buildDirectory.dir("smoke-test/java")

    inputs.file(launcher)
    outputs.dir(generatedDir)

    executable = launcher.get().asFile.absolutePath
    args("java", "--projects", "3", "--source-files", "2", "--dir", generatedDir.get().asFile.absolutePath)

    val settingsFile = generatedDir.map { it.file("settings.gradle").asFile }
    doLast {
        val settings = settingsFile.get()
        check(settings.isFile) {
            "The build-builder distribution exited successfully but generated no build: $settings is missing"
        }
    }
}

tasks.check {
    dependsOn(smokeTestInstalledDistribution)
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
