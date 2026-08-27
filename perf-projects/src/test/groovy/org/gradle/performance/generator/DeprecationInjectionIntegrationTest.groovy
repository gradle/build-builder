/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.performance.generator

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

/**
 * Moved from gradle/gradle, where it extended {@code AbstractIntegrationSpec}. The build is now run
 * through TestKit instead of that repository's integration-test executer; the assertions are
 * unchanged.
 */
class DeprecationInjectionIntegrationTest extends Specification {
    @TempDir
    File tmpDir

    def "generated project fires deprecationsPerProject x subProjects deprecations"() {
        given:
        def config = new TestProjectGeneratorConfigurationBuilder("largeJavaMultiProjectDeprecations", "largeJavaMultiProject")
            .withSubProjects(2)
            .withSourceFiles(1)
            .withDaemonMemory("512m")
            .withCompilerMemory("256m")
            .withDeprecationsPerProject(2)
            .create()
        new TestProjectGenerator(config).generate(tmpDir)
        def projectDir = new File(tmpDir, "largeJavaMultiProjectDeprecations")

        expect: "the plugin is generated and applied to each subproject via the plugins block"
        new File(projectDir, "buildSrc/src/main/java/perf/PerfDeprecationsPlugin.java").isFile()
        new File(projectDir, "project0/build.gradle").text.contains("id 'perf-deprecations'")

        when: "configuration runs with all warnings shown"
        def result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("help", "--warning-mode", "all")
            .withGradleVersion(gradleVersion)
            .forwardOutput()
            .build()

        then: "exactly deprecationsPerProject x subProjects deprecations fire"
        result.output.count("Perf deprecation from ") == 2 * 2
    }

    def "no deprecations plugin is generated when the flag is zero"() {
        given:
        def config = new TestProjectGeneratorConfigurationBuilder("largeJavaMultiProject")
            .withSubProjects(2)
            .withSourceFiles(1)
            .withDaemonMemory("512m")
            .withCompilerMemory("256m")
            .create()
        new TestProjectGenerator(config).generate(tmpDir)
        def projectDir = new File(tmpDir, "largeJavaMultiProject")

        expect:
        !new File(projectDir, "buildSrc/src/main/java/perf/PerfDeprecationsPlugin.java").exists()
        !new File(projectDir, "project0/build.gradle").text.contains("perf-deprecations")
    }

    /**
     * The emitted plugin calls {@code org.gradle.internal.deprecation.DeprecationLogger}, so this
     * test only means anything against a Gradle version that still has it. Kept aligned with the
     * version the other integration tests in this repository use.
     */
    private static String getGradleVersion() {
        return "9.0.0"
    }
}
