package org.gradle.builds.assemblers

import org.gradle.builds.generators.GeneratorVersions
import org.gradle.builds.model.GradlePluginComponent
import org.gradle.builds.model.JavaClass
import org.gradle.builds.model.Project


class GradlePluginModelAssembler : ComponentSpecificProjectConfigurer<GradlePluginComponent>(GradlePluginComponent::class.java) {
    override fun configure(settings: Settings, project: Project, component: GradlePluginComponent) {
        val buildScript = project.buildScript
        buildScript.requirePlugin("java-gradle-plugin")
        buildScript.mavenCentral()
        // Cap target compatibility so the jar stays readable by Gradle's
        // bundled ASM. Gradle 9.0 ships ASM that does not understand class file
        // major version 69 (Java 25), so a buildSrc compiled with the daemon
        // JVM (Java 25 here) fails the instrumentation transform.
        val javaBlock = buildScript.block("java")
        javaBlock.statement("sourceCompatibility = JavaVersion.VERSION_17")
        javaBlock.statement("targetCompatibility = JavaVersion.VERSION_17")
        buildScript.dependsOnExternal("testImplementation", GeneratorVersions.JUNIT4)

        val id = component.id!!
        val pos = id.lastIndexOf(".")
        val baseName = id.substring(pos + 1)
        val impl = project.qualifiedNamespaceFor + '.' + baseName.replaceFirstChar { it.uppercaseChar() } + "Plugin"
        component.implClass = JavaClass(impl)

        val block = buildScript.block("gradlePlugin").block("plugins").block(baseName)
        block.property("id", id)
        block.property("implementationClass", impl)
    }
}
