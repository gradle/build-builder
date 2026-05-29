package org.gradle.builds

import spock.lang.IgnoreIf

// @IgnoreIf has to live on the concrete class (Spock does not inherit it from
// an abstract superclass). See AbstractSwiftIntegrationTest for the rationale.
@IgnoreIf({ os.macOs })
class SwiftBuildBuildSrcIntegrationTest extends AbstractSwiftIntegrationTest {
    def "can generate buildsrc"() {
        when:
        new Main().run("swift", "--dir", projectDir.absolutePath, "--buildsrc")

        then:
        build.isBuild()

        def buildSrc = build(file("buildSrc"))
        buildSrc.isBuild()
        buildSrc.rootProject.isJavaPlugin()

        build.buildSucceeds(":show")
    }
}
