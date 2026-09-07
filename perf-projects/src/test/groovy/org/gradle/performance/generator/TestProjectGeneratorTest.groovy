/*
 * Copyright 2025 the original author or authors.
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

import spock.lang.Specification
import spock.lang.TempDir

class TestProjectGeneratorTest extends Specification {
    @TempDir
    File temporaryFolder

    /**
     * A small project with the same *shape* as `largeJavaMultiProjectHierarchy` — nested to
     * `projectDepth` levels, several subprojects, a few source files each.
     *
     * <p>These cases assert structure (nesting depth, subproject count, source files per project,
     * which build files exist), all of it read back from `config`, so they do not need real scale.
     * Using the production config generated 304,511 files four times over — about 1.2 million files
     * and seven minutes per run, enough to destabilise the rest of the build. Byte-for-byte fidelity
     * of the real projects is covered by diffing generated output against gradle/gradle, not here.
     */
    def config = new TestProjectGeneratorConfigurationBuilder("largeJavaMultiProjectHierarchy")
        .withSourceFiles(2)
        .withSubProjects(12)
        .withProjectDepth(5)
        .withDaemonMemory('2g')
        .withCompilerMemory('512m')
        .assembleChangeFile()
        .create()

    def generator = new TestProjectGenerator(config)

    File getOutputDir() {
        new File(temporaryFolder, "output").tap { mkdirs() }
    }

    def "generates project hierarchy with correct depth"() {
        when:
        generator.generate(outputDir)

        then:
        def rootDir = new File(outputDir, config.projectName)
        rootDir.exists()

        // Check project depth (should be 5 levels deep)
        def depthProject = rootDir.listFiles().find { it.name == "project0" }
        for (int i = 0; i < config.projectDepth; i++) {
            depthProject = new File(depthProject, "sub${i}project0")
            assert depthProject.exists()
        }

        // Verify no deeper levels exist
        !new File(depthProject, "sub${config.projectDepth}project0").exists()
    }

    def "generates correct number of subprojects"() {
        when:
        generator.generate(outputDir)

        then:
        def rootDir = new File(outputDir, config.projectName)
        def subprojectDirs = rootDir.listFiles().findAll { it.isDirectory() && it.name.startsWith("project") }
        subprojectDirs.size() == config.subProjects
    }

    def "generates correct number of source files per project"() {
        when:
        generator.generate(outputDir)

        then:
        def rootDir = new File(outputDir, config.projectName)
        def sampleProject = new File(rootDir, "project5")

        def productionFiles = findSourceFiles(sampleProject, "src/main/${config.language.name}")
        productionFiles.size() == config.sourceFiles

        def testFiles = findSourceFiles(sampleProject, "src/test/${config.language.name}")
        testFiles.size() == config.sourceFiles
    }

    def "generates expected gradle files"() {
        when:
        generator.generate(outputDir)

        then:
        def rootDir = new File(outputDir, config.projectName)
        new File(rootDir, config.dsl.fileNameFor('build')).exists()
        new File(rootDir, config.dsl.fileNameFor('settings')).exists()
        new File(rootDir, "gradle.properties").exists()

        def subproject = new File(rootDir, "project10")
        new File(subproject, config.dsl.fileNameFor('build')).exists()
    }

    private List<File> findSourceFiles(File dir, String path) {
        def sourceDir = new File(dir, path)
        if (!sourceDir.exists()) {
            return []
        }

        def result = []
        sourceDir.eachFileRecurse { file ->
            if (file.isFile() && (file.name.endsWith(".java") || file.name.endsWith(".groovy") || file.name.endsWith(".kt"))) {
                result << file
            }
        }
        return result
    }
}
