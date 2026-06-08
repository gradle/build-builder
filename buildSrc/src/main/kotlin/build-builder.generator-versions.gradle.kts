/**
 * Code-generates `org.gradle.builds.generators.GeneratorVersions` from every
 * version-catalog library alias prefixed with `generator-`.
 *
 * The generator emitters in build-builder inject these coordinates verbatim
 * into the generated `build.gradle` files. Surfacing them as catalog entries
 * lets automation drive version bumps.
 *
 * Naming convention: `generator-androidx-test-runner` → constant
 * `ANDROIDX_TEST_RUNNER` plus `ANDROIDX_TEST_RUNNER_VERSION`. The prefix is
 * stripped, the rest is upper-snake-cased.
 */
plugins {
    `java-base`
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val tomlFile = rootProject.layout.projectDirectory.file("gradle/libs.versions.toml").asFile

data class CatalogEntry(val constant: String, val coord: String, val version: String)

val entries: List<CatalogEntry> = catalog.libraryAliases
    .filter { it.startsWith("generator.") }
    .sorted()
    .map { alias ->
        val library = catalog.findLibrary(alias).get().get()
        val module = library.module
        val version = library.versionConstraint.requiredVersion
        CatalogEntry(
            constant = alias.removePrefix("generator.").replace('.', '_').replace('-', '_').uppercase(),
            coord = "${module.group}:${module.name}",
            version = version,
        )
    }

require(entries.isNotEmpty()) {
    "No `generator-*` entries found in libs.versions.toml [libraries] — buildSrc/build-builder.generator-versions would emit an empty GeneratorVersions class"
}

val generateGeneratorVersions by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/sources/generator-versions/java/main")
    outputs.dir(outputDir)
    inputs.file(tomlFile)
    val captured = entries
    doLast {
        val outFile = outputDir.get().asFile.resolve("org/gradle/builds/generators/GeneratorVersions.java")
        outFile.parentFile.mkdirs()
        outFile.writeText(buildString {
            appendLine("// GENERATED FILE — do not edit. Sourced from gradle/libs.versions.toml")
            appendLine("// via the build-builder.generator-versions buildSrc plugin.")
            appendLine("package org.gradle.builds.generators;")
            appendLine()
            appendLine("public final class GeneratorVersions {")
            captured.forEach {
                appendLine("    public static final String ${it.constant}_VERSION = \"${it.version}\";")
            }
            appendLine()
            captured.forEach {
                appendLine("    /** {@code ${it.coord}:${it.version}} */")
                appendLine("    public static final String ${it.constant} = \"${it.coord}:\" + ${it.constant}_VERSION;")
            }
            appendLine()
            appendLine("    private GeneratorVersions() {}")
            appendLine("}")
        })
    }
}

extensions.getByType<SourceSetContainer>().named("main") {
    java.srcDir(generateGeneratorVersions)
}
