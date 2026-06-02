package org.gradle.builds.generators

import org.gradle.builds.model.AndroidComponent
import org.gradle.builds.model.ConfiguredBuild
import org.gradle.builds.model.HasHeapRequirements
import kotlin.math.max

class GradlePropertiesGenerator : Generator<ConfiguredBuild> {
    override fun generate(build: ConfiguredBuild, fileGenerator: FileGenerator) {
        val readMe = build.rootDir.resolve("gradle.properties")
        val component = build.rootProject.component(HasHeapRequirements::class.java)
        val min = component?.minHeapMegabytes ?: 128
        val size = max(min, build.projects.size * 4)
        val hasAndroid = build.projects.any { it.component(AndroidComponent::class.java) != null }
        fileGenerator.generate(readMe) { writer ->
            writer.println("org.gradle.jvmargs=-Xmx${size}m")
            if (hasAndroid) {
                // AGP 8 refuses to build when AndroidX dependencies are on the
                // classpath without these flags explicitly opted in.
                writer.println("android.useAndroidX=true")
                writer.println("android.enableJetifier=false")
            }
        }
    }
}
