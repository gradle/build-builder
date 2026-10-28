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
    // gradle/gradle's named performance test projects, moved over from that repository. Kept in
    // Groovy on purpose: rewriting them at the same time as moving them would have put recorded
    // performance baselines at risk for no benefit. See perf-projects/build.gradle.kts.
    implementation(project(":perf-projects"))

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

java {
    // Sets the Groovy compiler's bytecode target. GroovyCompile has no working equivalent of
    // `--release` — setting `options.release` on it is silently ignored, even for Java sources
    // under src/main/groovy — so Groovy-compiled code is still checked against the build JVM's
    // class library. Only JavaCompile, below, enforces the API level; keep Java in src/main/java.
    sourceCompatibility = JavaVersion.toVersion(distributionJvmTarget)
    targetCompatibility = JavaVersion.toVersion(distributionJvmTarget)
}

tasks.withType<JavaCompile>().configureEach {
    options.release = distributionJvmTarget
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(distributionJvmTarget.toString())
    }
}

// gradle/gradle runs `bin/build-builder` from the installed distribution: `BuildBuilderGenerator`
// for its Swift performance-test projects, and `perf-project` for the named Java ones. Nothing else
// in this build exercises that path, so breakage in it — a wrong install task name, Groovy missing
// from the runtime classpath, an unrunnable class file version — only ever surfaces on the
// consumer's CI. Cover it here instead, on the oldest JVM the distribution promises to run on.
val smokeTestJvm = javaToolchains.launcherFor {
    languageVersion = JavaLanguageVersion.of(distributionJvmTarget)
}

fun registerInstalledDistributionSmokeTest(
    name: String,
    description: String,
    expectedOutput: String,
    vararg cliArgs: String,
) = tasks.register<Exec>(name) {
    this.description = description
    group = LifecycleBasePlugin.VERIFICATION_GROUP

    dependsOn(tasks.installDist)

    val launcher = layout.buildDirectory.file("install/${project.name}/bin/${project.name}")
    val generatedDir = layout.buildDirectory.dir("smoke-test/$name")
    val javaHome = smokeTestJvm.map { it.metadata.installationPath.asFile.absolutePath }

    inputs.dir(tasks.installDist.map { it.destinationDir })
    inputs.property("javaVersion", distributionJvmTarget)
    outputs.dir(generatedDir)

    executable = launcher.get().asFile.absolutePath
    args(*cliArgs, "--dir", generatedDir.get().asFile.absolutePath)

    val expectedFile = generatedDir.map { it.file(expectedOutput).asFile }
    doFirst {
        // The launcher script picks its JVM from JAVA_HOME.
        environment("JAVA_HOME", javaHome.get())
    }
    doLast {
        val expected = expectedFile.get()
        check(expected.isFile) {
            "The build-builder distribution exited successfully but generated no build: $expected is missing"
        }
    }
}

val smokeTestInstalledDistribution = registerInstalledDistributionSmokeTest(
    "smokeTestInstalledDistribution",
    "Runs the CLI from the installed distribution, the way gradle/gradle consumes it.",
    "settings.gradle",
    "java", "--projects", "3", "--source-files", "2",
)

val smokeTestInstalledPerfProject = registerInstalledDistributionSmokeTest(
    "smokeTestInstalledPerfProject",
    "Generates a named performance test project from the installed distribution.",
    "smallJavaMultiProject/perf-project.json",
    "perf-project", "smallJavaMultiProject",
)

tasks.check {
    dependsOn(smokeTestInstalledDistribution, smokeTestInstalledPerfProject)
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
