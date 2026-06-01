package org.gradle.builds

class KotlinBuildIntegrationTest extends AbstractIntegrationTest {
    def "can generate Kotlin application"() {
        when:
        new Main().run("kotlin", "--dir", projectDir.absolutePath)

        then:
        build.isBuild()
        build.rootProject.isKotlinApplication()

        build.buildSucceeds(":installDist")

        def app = build.app("build/install/testApp/bin/testApp")
        app.isApp()
        baseNames(app.libDir.list()) == ["kotlin-stdlib", "kotlin-stdlib-jdk7", "kotlin-stdlib-jdk8", "annotations", "testApp"] as Set
        app.succeeds()

        build.buildSucceeds("build")
    }

    def "can generate 3 project Kotlin application"() {
        when:
        new Main().run("kotlin", "--dir", projectDir.absolutePath, "--projects", "3")

        then:
        build.isBuild()
        def rootProject = build.rootProject.isKotlinApplication()
        def libApi = build.project(":libapi").isKotlinLibrary()
        def libCore = build.project(":libcore").isKotlinLibrary()

        rootProject.dependsOn(libApi)
        libApi.dependsOn(libCore)
        libCore.dependsOn()

        build.buildSucceeds(":installDist")

        def app = build.app("build/install/testApp/bin/testApp")
        app.isApp()
        baseNames(app.libDir.list()) == ["kotlin-stdlib", "kotlin-stdlib-jdk7", "kotlin-stdlib-jdk8", "annotations", "testApp", "libapi", "libcore"] as Set
        app.succeeds()

        build.buildSucceeds("build")
    }
}
