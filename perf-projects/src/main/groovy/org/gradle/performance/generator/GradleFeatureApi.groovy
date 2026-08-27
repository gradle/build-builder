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
 * Fully-qualified names of the Gradle project-feature API types referenced by the ecosystem plugin
 * sources that {@link DeclarativeDslTestProjectGenerator} emits.
 *
 * <p>In gradle/gradle these were {@code Class} references imported from {@code org.gradle.features.*},
 * which acted as a compile-time canary: renaming the API broke the generator immediately.
 * build-builder does not compile against the Gradle API, so they are literals here. Drift now
 * surfaces one step later — the generated {@code largeEmptyMultiProjectDeclarativeDsl} project fails
 * to compile when its performance scenario runs.
 */
@CompileStatic
class GradleFeatureApi {
    static final String REGISTERS_PROJECT_FEATURES = 'org.gradle.features.annotations.RegistersProjectFeatures'
    static final String BINDS_PROJECT_TYPE = 'org.gradle.features.annotations.BindsProjectType'
    static final String BUILD_MODEL = 'org.gradle.features.binding.BuildModel'
    static final String DEFINITION = 'org.gradle.features.binding.Definition'
    static final String PROJECT_FEATURE_APPLICATION_CONTEXT = 'org.gradle.features.binding.ProjectFeatureApplicationContext'
    static final String PROJECT_TYPE_APPLY_ACTION = 'org.gradle.features.binding.ProjectTypeApplyAction'
    static final String PROJECT_TYPE_BINDING = 'org.gradle.features.binding.ProjectTypeBinding'
    static final String PROJECT_TYPE_BINDING_BUILDER = 'org.gradle.features.binding.ProjectTypeBindingBuilder'
}
