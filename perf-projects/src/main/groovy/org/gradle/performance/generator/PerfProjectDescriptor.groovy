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

import groovy.json.JsonOutput
import groovy.transform.CompileStatic

/**
 * Writes the handful of generator settings that the *consumer's* performance tests need at run time.
 *
 * <p>In gradle/gradle the test fixtures reached straight into the generator:
 * {@code JavaTestProject} and several performance specs read
 * {@code testProject.config.fileToChangeByScenario['assemble']}, {@code config.language} and
 * {@code daemonMemory} off the {@code JavaTestProjectGenerator} enum. That made the generator both
 * the thing that produces projects and the fixtures' source of truth, which is why it could not
 * simply be lifted out of the repository.
 *
 * <p>Emitting this file next to the generated project breaks that: the generator stays here, and the
 * consumer reads a descriptor instead of importing an enum. It is written to
 * {@code <project>/perf-project.json}.
 *
 * <p>The file sits outside every path the generated build itself references, so adding it does not
 * change what the generated build does or how long it takes.
 */
@CompileStatic
class PerfProjectDescriptor {
    static final String FILE_NAME = "perf-project.json"

    static void write(File projectDir, TestProjectGeneratorConfiguration config) {
        Map<String, Object> descriptor = [
            projectName          : config.projectName,
            templateName         : config.templateName,
            language             : config.language.name(),
            dsl                  : config.dsl.name(),
            daemonMemory         : config.daemonMemory,
            compilerMemory       : config.compilerMemory,
            testRunnerMemory     : config.testRunnerMemory,
            parallel             : config.parallel,
            maxWorkers           : config.maxWorkers,
            maxParallelForks     : config.maxParallelForks,
            subProjects          : config.subProjects,
            sourceFiles          : config.sourceFiles,
            useTestNG            : config.useTestNG,
            fileToChangeByScenario: config.fileToChangeByScenario,
        ] as Map<String, Object>

        new File(projectDir, FILE_NAME).text = JsonOutput.prettyPrint(JsonOutput.toJson(descriptor)) + "\n"
    }
}
