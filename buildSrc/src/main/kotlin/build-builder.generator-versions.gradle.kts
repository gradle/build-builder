/**
 * Code-generates `org.gradle.builds.generators.GeneratorVersions` from every
 * version-catalog library alias prefixed with `generator-`.
 *
 * The generator emitters in build-builder inject these coordinates verbatim
 * into the generated `build.gradle` files (AGP, JUnit 4, AndroidX, the Kotlin
 * Gradle plugin, …). Surfacing them as catalog entries lets Dependabot drive
 * version bumps; this plugin closes the loop so the bumps land in compiled
 * code without manual mirroring.
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

// VersionCatalog.libraryAliases returns names in dot-normalized form, so a
// TOML alias `generator-junit4` appears here as `generator.junit4`. Filter on
// that prefix and convert back to an upper-snake-case Java constant name.
val entries: List<CatalogEntry> = catalog.libraryAliases
    .filter { it.startsWith("generator.") }
    .sorted()
    .map { alias ->
        val library = catalog.findLibrary(alias).get().get()
        val module = library.module
        val version = library.versionConstraint.requiredVersion
        CatalogEntry(
            constant = alias.removePrefix("generator.").replace('.', '_').replace('-', '_').toUpperCase(),
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
            appendLine("// via the build-builder.generator-versions buildSrc plugin. Dependabot")
            appendLine("// updates the TOML; the next build regenerates this class.")
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
