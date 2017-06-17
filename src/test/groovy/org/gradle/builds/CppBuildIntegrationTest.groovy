package org.gradle.builds

import spock.lang.Unroll

class CppBuildIntegrationTest extends AbstractIntegrationTest {
    def setup() {
        gradleVersion = "4.1-20170607235835+0000"
    }

    def "can generate single project build"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath)

        then:
        build.isBuild()
        build.project(":").isCppProject()
        build.project(":").file("src/main/headers").list() as Set == ["app.h"] as Set
        def srcDir = build.project(":").file("src/main/cpp")
        srcDir.list() as Set == ["app.cpp", "app_impl1_1.cpp", "app_nodeps1.cpp"] as Set
        new File(srcDir, "app.cpp").text.contains("AppImpl1_1")
        new File(srcDir, "app_impl1_1.cpp").text.contains("AppNoDeps1")

        build.buildSucceeds(":installMain")
        build.app("build/install/testApp/testApp").succeeds()

        build.buildSucceeds("build")
    }

    @Unroll
    def "can generate single project build with #sourceFiles source files"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--source-files", sourceFiles)

        then:
        build.isBuild()
        build.project(":").isCppProject()

        build.buildSucceeds(":installMain")
        build.app("build/install/testApp/testApp").succeeds()

        build.buildSucceeds("build")

        where:
        sourceFiles << ["1", "2", "5"]
    }

    def "can generate 2 project build"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "2")

        then:
        build.isBuild()
        build.project(":").isCppProject()
        build.project(":").file("src/main/headers").list() as Set == ["app.h"] as Set
        def srcDir = build.project(":").file("src/main/cpp")
        srcDir.list() as Set == ["app.cpp", "app_impl1_1.cpp", "app_nodeps1.cpp"] as Set
        new File(srcDir, "app_impl1_1.cpp").text.contains("Core1")

        build.project(":core1").isCppProject()
        build.project(":core1").file("src/main/public").list() as Set == ["core1.h"] as Set
        build.project(":core1").file("src/main/headers").list() as Set == ["core1_impl.h"] as Set
        build.project(":core1").file("src/main/cpp").list() as Set == ["core1.cpp", "core1_impl1_1.cpp", "core1_nodeps1.cpp"] as Set

        build.buildSucceeds(":installMain")
        build.app("build/install/testApp/testApp").succeeds()

        build.buildSucceeds("build")
    }

    @Unroll
    def "can generate #projects project build"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", projects)

        then:
        build.isBuild()
        build.project(":").isCppProject()
        build.project(":").file("src/main/headers").list() as Set == ["app.h"] as Set
        build.project(":").file("src/main/cpp").list() as Set == ["app.cpp", "app_impl1_1.cpp", "app_nodeps1.cpp"] as Set

        build.project(":lib1_1").isCppProject()
        build.project(":lib1_1").file("src/main/public").list() as Set == ["lib1_1.h"] as Set
        build.project(":lib1_1").file("src/main/headers").list() as Set == ["lib1_1_impl.h"] as Set
        build.project(":lib1_1").file("src/main/cpp").list() as Set == ["lib1_1.cpp", "lib1_1_impl1_1.cpp", "lib1_1_nodeps1.cpp"] as Set

        build.project(":core1").isCppProject()
        build.project(":core1").file("src/main/public").list() as Set == ["core1.h"] as Set
        build.project(":core1").file("src/main/headers").list() as Set == ["core1_impl.h"] as Set
        build.project(":core1").file("src/main/cpp").list() as Set == ["core1.cpp", "core1_impl1_1.cpp", "core1_nodeps1.cpp"] as Set

        build.buildSucceeds(":installMain")
        build.app("build/install/testApp/testApp").succeeds()

        build.buildSucceeds("build")

        where:
        projects << ["3", "4", "5"]
    }

    @Unroll
    def "can generate multi-project build with #sourceFiles source files"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "4", "--source-files", sourceFiles)

        then:
        build.isBuild()
        build.project(":").isCppProject()
        build.project(":lib1_1").isCppProject()
        build.project(":lib1_2").isCppProject()
        build.project(":core1").isCppProject()

        build.buildSucceeds(":installMain")
        build.app("build/install/testApp/testApp").succeeds()

        build.buildSucceeds("build")

        where:
        sourceFiles << ["1", "2", "5"]
    }
}
