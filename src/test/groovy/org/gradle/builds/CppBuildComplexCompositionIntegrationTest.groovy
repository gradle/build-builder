package org.gradle.builds

class CppBuildComplexCompositionIntegrationTest extends AbstractIntegrationTest {
    def "can generate build"() {
        given:
        useIsolatedUserHome()

        when:
        new Main().run("cpp", "--http-repo", "--included-builds", "2", "--source-dep-builds", "2", "--dir", projectDir.absolutePath)

        then:
        build.isBuild()

        build(file('external/v1')).isBuild()
        build(file('external/sourceApi')).isBuild()
        build(file('external/sourceCore')).isBuild()
        build(file('childApi')).isBuild()
        build(file('childCore')).isBuild()

        build.buildSucceeds(":publishHttpRepo")
        build.buildSucceeds(":installDebug")

        def app = build.app("build/install/main/debug/testApp")
        app.succeeds()

        build.buildSucceeds("build")
    }

}
