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

import groovy.transform.CompileStatic

/**
 * Single entry point for generating one of gradle/gradle's named performance test projects.
 *
 * <p>Picks the right generator for the project's DSL and writes the consumer-facing descriptor
 * alongside it.
 */
@CompileStatic
class PerfProjects {
    static List<String> projectNames() {
        JavaTestProjectGenerator.values().collect { JavaTestProjectGenerator it -> it.projectName }
    }

    static File generate(String projectName, File outputBaseDir, String repositoryUrl = null) {
        JavaTestProjectGenerator project = JavaTestProjectGenerator.values().find {
            JavaTestProjectGenerator candidate -> candidate.projectName == projectName
        }
        if (project == null) {
            throw new IllegalArgumentException(
                "Unknown performance test project '${projectName}'. Known projects: ${projectNames().sort().join(', ')}")
        }

        TestProjectGeneratorConfiguration config = project.config
        if (repositoryUrl) {
            config.repositories = [RepositoryDefinitions.mavenRepositoryDefinition(config.dsl, repositoryUrl)] as String[]
        }

        AbstractTestProjectGenerator generator = config.dsl == GradleDsl.DECLARATIVE
            ? new DeclarativeDslTestProjectGenerator(config)
            : new TestProjectGenerator(config)
        generator.generate(outputBaseDir)

        File projectDir = new File(outputBaseDir, projectName)
        PerfProjectDescriptor.write(projectDir, config)
        return projectDir
    }
}
