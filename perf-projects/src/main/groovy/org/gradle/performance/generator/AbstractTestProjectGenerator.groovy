/*
 * Copyright 2024 the original author or authors.
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

abstract class AbstractTestProjectGenerator {

    /**
     * Generates the project into {@code <outputBaseDir>/<projectName>}.
     *
     * <p>Both subclasses already had this method; it is declared here so that callers can pick a
     * generator by DSL and invoke it without giving up static compilation.
     */
    abstract def generate(File outputBaseDir)

    protected static void file(File dir, String name, String content) {
        if (content == null) {
            return
        }
        def file = new File(dir, name)
        file.parentFile.mkdirs()
        file.setText(content.stripIndent().trim())
    }

}
