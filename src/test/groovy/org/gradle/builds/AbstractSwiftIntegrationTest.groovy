package org.gradle.builds

import spock.lang.IgnoreIf

/**
 * Common base for Swift integration tests.
 *
 * The Swift suites are skipped on macOS: Gradle's incubating
 * swift-application / swift-library / xctest plugins do not produce a usable
 * test executable against the Xcode 26.x toolchain (relocateMainForTest no
 * longer extracts {@code _main}, and macOS 26's SDK marks SwiftUICore as a
 * restricted framework that only Apple-signed binaries may link).
 */
@IgnoreIf({ os.macOs })
abstract class AbstractSwiftIntegrationTest extends AbstractIntegrationTest {
}
