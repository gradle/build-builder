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
 * Emits the {@code repositories { }} entry for generated projects.
 *
 * <p>This reproduces, byte for byte, what {@code RepoScriptBlockUtil.mavenCentralRepositoryDefinition}
 * in gradle/gradle produced for these projects, so that generated output — and therefore recorded
 * performance baselines — does not shift as a result of the move to build-builder.
 *
 * <p>Two details of the original are deliberately preserved:
 *
 * <ul>
 * <li>The repository is named {@code MAVEN_CENTRAL_MIRROR} even when it points at Maven Central
 *     itself. In the original, the mirror URL defaulted to the original URL, which made the
 *     "is there a mirror?" test always true, so the {@code _MIRROR} suffix was always appended.
 * <li>The URL is plain Maven Central. The original read the mirror from the
 *     {@code org.gradle.integtest.mirrors.mavencentral} system property, but gradle/gradle only
 *     injects that into {@code Test} tasks, never into the generator's {@code JavaExec} — so at
 *     generation time the property was always absent. Consumers that do want a mirror baked in can
 *     now pass {@code --repository-url}, which is an explicit input rather than ambient state.
 * </ul>
 */
@CompileStatic
class RepositoryDefinitions {
    static final String MAVEN_CENTRAL_URL = "https://repo.maven.apache.org/maven2/"

    static String mavenRepositoryDefinition(GradleDsl dsl, String url = MAVEN_CENTRAL_URL) {
        repositoryDefinition(dsl, "maven", "MAVEN_CENTRAL_MIRROR", url)
    }

    static String repositoryDefinition(GradleDsl dsl, String type, String name, String url) {
        if (dsl == GradleDsl.KOTLIN) {
            """
                    ${type} {
                        name = "${name}"
                        url = uri("${url}")
                    }
                """
        } else {
            """
                    ${type} {
                        name = '${name}'
                        url = '${url}'
                    }
                """
        }
    }
}
