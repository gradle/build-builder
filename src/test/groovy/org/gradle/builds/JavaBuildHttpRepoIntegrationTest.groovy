package org.gradle.builds

class JavaBuildHttpRepoIntegrationTest extends AbstractIntegrationTest {
    def "can generate single project build with http repo"() {
        given:
        useIsolatedUserHome()

        when:
        new Main().run("java", "--http-repo", "--dir", projectDir.absolutePath)

        then:
        build.isBuild()

        def rootProject = build.rootProject.isJavaApplication()

        def buildFile = rootProject.file("build.gradle")
        buildFile.text.contains("implementation 'org.gradle.example:extlibapi1:1.0.0'")
        buildFile.text.contains("implementation 'org.gradle.example:extlibapi2:1.0.0'")

        def repoBuild = build(file('external/v1'))
        repoBuild.isBuild()
        repoBuild.project(':').isEmptyProject()
        def lib1 = repoBuild.project(':extlibapi1').isJavaLibrary()
        def lib2 = repoBuild.project(':extlibapi2').isJavaLibrary()
        def lib3 = repoBuild.project(':extlibcore').isJavaLibrary()

        rootProject.dependsOn(lib1, lib2)
        lib1.dependsOn(lib3)
        lib2.dependsOn(lib3)
        lib3.dependsOn()

        build.buildSucceeds(":publishHttpRepo")
        build.buildSucceeds(":installDist")

        file("http-repo/org/gradle/example/extlibcore/1.0.0/extlibcore-1.0.0.pom").file
        file("http-repo/org/gradle/example/extlibcore/1.0.0/extlibcore-1.0.0.jar").file
        file("http-repo/org/gradle/example/extlibapi2/1.0.0/extlibapi2-1.0.0.pom").file
        file("http-repo/org/gradle/example/extlibapi2/1.0.0/extlibapi2-1.0.0.jar").file
        file("http-repo/org/gradle/example/extlibapi1/1.0.0/extlibapi1-1.0.0.pom").file
        file("http-repo/org/gradle/example/extlibapi1/1.0.0/extlibapi1-1.0.0.jar").file

        def app = build.app("build/install/testApp/bin/testApp")
        app.isApp()
        baseNames(app.libDir.list()) == ["extlibapi1", "extlibapi2", "extlibcore", "slf4j-api", "slf4j-simple", "testApp"] as Set
        app.succeeds()

        build.buildSucceeds("build")
    }

    def "can generate single project build with http repo with single library"() {
        given:
        useIsolatedUserHome()

        when:
        new Main().run("java", "--http-repo", "--http-repo-libraries", "1", "--dir", projectDir.absolutePath)

        then:
        build.isBuild()

        def rootProject = build.rootProject.isJavaApplication()

        def repoBuild = build(file('external/v1'))
        repoBuild.isBuild()
        def lib = repoBuild.project(':').isJavaLibrary()

        rootProject.dependsOn(lib)
        lib.dependsOn()

        build.buildSucceeds(":publishHttpRepo")
        build.buildSucceeds(":installDist")

        file("http-repo/org/gradle/example/ext/1.0.0/ext-1.0.0.pom").file
        file("http-repo/org/gradle/example/ext/1.0.0/ext-1.0.0.jar").file

        def app = build.app("build/install/testApp/bin/testApp")
        app.isApp()
        baseNames(app.libDir.list()) == ["ext", "slf4j-api", "slf4j-simple", "testApp"] as Set
        app.succeeds()

        build.buildSucceeds("build")
    }

    def "can generate multi-project build with http repo"() {
        given:
        useIsolatedUserHome()

        when:
        new Main().run("java", "--http-repo", "--projects", "3", "--dir", projectDir.absolutePath)

        then:
        build.isBuild()
        def rootProject = build.rootProject.isJavaApplication()
        def lib1 = build.project(":libapi").isJavaLibrary()
        def lib2 = build.project(":libcore").isJavaLibrary()

        def repoBuild = build(file('external/v1'))
        repoBuild.isBuild()
        repoBuild.project(':').isEmptyProject()
        def extlib1 = repoBuild.project(':extlibapi1').isJavaLibrary()
        def extlib2 = repoBuild.project(':extlibapi2').isJavaLibrary()
        def extlib3 = repoBuild.project(':extlibcore').isJavaLibrary()

        rootProject.dependsOn(lib1, extlib1, extlib2)
        lib1.dependsOn(lib2, extlib1, extlib2)
        lib2.dependsOn(extlib1, extlib2)
        extlib1.dependsOn(extlib3)
        extlib2.dependsOn(extlib3)
        extlib3.dependsOn()

        build.buildSucceeds(":publishHttpRepo")
        build.buildSucceeds(":installDist")

        file("http-repo/org/gradle/example/extlibcore/1.0.0/extlibcore-1.0.0.pom").file
        file("http-repo/org/gradle/example/extlibcore/1.0.0/extlibcore-1.0.0.jar").file

        def app = build.app("build/install/testApp/bin/testApp")
        app.isApp()
        baseNames(app.libDir.list()) == ["extlibapi1", "extlibapi2", "extlibcore", "libapi", "libcore", "slf4j-api", "slf4j-simple", "testApp"] as Set
        app.succeeds()

        build.buildSucceeds("build")
    }

    def "can generate single project build with http repo with multiple versions"() {
        given:
        useIsolatedUserHome()

        when:
        new Main().run("java", "--http-repo", "--http-repo-versions", "3", "--dir", projectDir.absolutePath)

        then:
        build.isBuild()

        def rootProject = build.rootProject.isJavaApplication()

        def buildFile = rootProject.file("build.gradle")
        buildFile.text.contains("implementation 'org.gradle.example:extlibapi1:3.0.0'")
        buildFile.text.contains("implementation 'org.gradle.example:extlibapi2:3.0.0'")

        def repoBuild = build(file('external/v1'))
        repoBuild.isBuild()
        repoBuild.project(':').isEmptyProject()
        def lib1 = repoBuild.project(':extlibapi1').isJavaLibrary()
        def lib2 = repoBuild.project(':extlibapi2').isJavaLibrary()
        def lib3 = repoBuild.project(':extlibcore').isJavaLibrary()

        rootProject.dependsOn(lib1, lib2)
        lib1.dependsOn(lib3)
        lib2.dependsOn(lib3)
        lib3.dependsOn()

        build.buildSucceeds(":publishHttpRepo")
        build.buildSucceeds(":installDist")

        file("http-repo/org/gradle/example/extlibapi1/1.0.0/extlibapi1-1.0.0.pom").file
        file("http-repo/org/gradle/example/extlibapi1/1.0.0/extlibapi1-1.0.0.jar").file
        file("http-repo/org/gradle/example/extlibapi2/2.0.0/extlibapi2-2.0.0.pom").file
        file("http-repo/org/gradle/example/extlibapi2/2.0.0/extlibapi2-2.0.0.jar").file
        file("http-repo/org/gradle/example/extlibcore/3.0.0/extlibcore-3.0.0.pom").file
        file("http-repo/org/gradle/example/extlibcore/3.0.0/extlibcore-3.0.0.jar").file

        def app = build.app("build/install/testApp/bin/testApp")
        app.isApp()
        def installedLibs = app.libDir.list()
        baseNames(installedLibs) == ["extlibapi1", "extlibapi2", "extlibcore", "slf4j-api", "slf4j-simple", "testApp"] as Set
        // The whole point of this test: the consumer must resolve the highest published version, not 1.0.0 or 2.0.0.
        installedLibs.toList().containsAll(["extlibapi1-3.0.0.jar", "extlibapi2-3.0.0.jar", "extlibcore-3.0.0.jar"])
        app.succeeds()

        build.buildSucceeds("build")
    }

}
