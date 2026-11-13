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

import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.TempDir

/**
 * Covers the entry point the {@code perf-project} command calls. Uses {@code smallJavaMultiProject},
 * the smallest named project, so that a real enum constant is generated end to end.
 */
class PerfProjectsTest extends Specification {
    static final String PROJECT = "smallJavaMultiProject"

    @TempDir
    File tmpDir

    def "writes a descriptor matching the named project's configuration"() {
        when:
        def projectDir = PerfProjects.generate(PROJECT, tmpDir)

        then:
        projectDir == new File(tmpDir, PROJECT)
        def config = JavaTestProjectGenerator.SMALL_JAVA_MULTI_PROJECT.config
        def descriptor = new JsonSlurper().parse(new File(projectDir, PerfProjectDescriptor.FILE_NAME)) as Map
        descriptor.projectName == PROJECT
        descriptor.templateName == config.templateName
        descriptor.language == config.language.name()
        descriptor.dsl == config.dsl.name()
        descriptor.daemonMemory == config.daemonMemory
        descriptor.subProjects == config.subProjects
        descriptor.sourceFiles == config.sourceFiles
        descriptor.fileToChangeByScenario == config.fileToChangeByScenario
        new File(projectDir, descriptor.fileToChangeByScenario['assemble'] as String).isFile()
    }

    def "bakes a repository URL into the generated build without changing later generations"() {
        given:
        def mirror = "https://mirror.example.com/maven2/"

        when:
        def mirrored = PerfProjects.generate(PROJECT, new File(tmpDir, "mirrored"), mirror)
        def plain = PerfProjects.generate(PROJECT, new File(tmpDir, "plain"))

        then:
        def mirroredBuild = new File(mirrored, "project0/build.gradle").text
        mirroredBuild.contains("url = '${mirror}'")
        !mirroredBuild.contains(RepositoryDefinitions.MAVEN_CENTRAL_URL)

        def plainBuild = new File(plain, "project0/build.gradle").text
        plainBuild.contains("url = '${RepositoryDefinitions.MAVEN_CENTRAL_URL}'")
        !plainBuild.contains(mirror)
    }

    def "rejects an unknown project name, listing the known ones"() {
        when:
        PerfProjects.generate("noSuchProject", tmpDir)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("noSuchProject")
        e.message.contains(PROJECT)
    }
}
