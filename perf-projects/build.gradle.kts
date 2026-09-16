/**
 * gradle/gradle's named performance test projects.
 *
 * These generators moved here from
 * `testing/internal-performance-testing/src/main/groovy/org/gradle/performance/generator`.
 * They are a separate module rather than part of the root project for two reasons:
 *
 *  - they are Groovy, and the root project's `Main` is Java, which cannot see Groovy classes
 *    compiled into the same source set;
 *  - they carry a different contract from the rest of build-builder. build-builder generates
 *    builds to a shape you ask for; these generate *specific, named* projects whose output must
 *    stay stable, because performance results are keyed on the project name and any change in
 *    generated content resets recorded baselines.
 */
plugins {
    id("groovy")
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    // `api`: the generated classes implement GroovyObject, so consumers compiling against
    // them (the root project's `Main`) need Groovy on their compile classpath too.
    api(libs.groovy)
    implementation(libs.groovy.json)
}

// `useSpock` rather than a bare `testImplementation(libs.spock.core)` plus `useJUnitPlatform()`:
// the latter compiles but leaves no JUnit Platform engine on the runtime classpath, and the test
// task dies with TestFrameworkNotAvailableException. Matches the root project's test suite.
testing {
    suites {
        getByName<JvmTestSuite>("test") {
            useSpock(libs.versions.spock)
            dependencies {
                // DeprecationInjectionIntegrationTest runs a real build against the generated project.
                implementation(gradleTestKit())
            }
            targets {
                all {
                    testTask.configure {
                        // The generated buildSrc is compiled by whatever JVM runs the TestKit build,
                        // so the Gradle running it has to be new enough to instrument that bytecode.
                        // Tracking the wrapper keeps this correct across wrapper bumps.
                        systemProperty("testGradleVersion", gradle.gradleVersion)
                    }
                }
            }
        }
    }
}

val distributionJvmTarget = 17

java {
    sourceCompatibility = JavaVersion.toVersion(distributionJvmTarget)
    targetCompatibility = JavaVersion.toVersion(distributionJvmTarget)
}

tasks.withType<JavaCompile>().configureEach {
    options.release = distributionJvmTarget
}
