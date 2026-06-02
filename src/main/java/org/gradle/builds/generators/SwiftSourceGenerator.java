package org.gradle.builds.generators;

import org.gradle.builds.model.*;

import java.io.IOException;
import java.nio.file.Path;

public class SwiftSourceGenerator extends ProjectComponentSpecificGenerator<HasSwiftSource> {
    public SwiftSourceGenerator() {
        super(HasSwiftSource.class);
    }

    @Override
    protected void generate(ConfiguredBuild build, ConfiguredProject project, HasSwiftSource component, FileGenerator fileGenerator) throws IOException {
        Path srcDir = component.isSwiftPm() ? build.getRootDir().resolve("Sources/" + project.getName()) : project.getProjectDir().resolve("src/main/swift/");
        for (SwiftSourceFile swiftSource : component.getSourceFiles()) {
            generateSourceFile(srcDir, swiftSource, fileGenerator);
        }

        Path testDir = component.isSwiftPm() ? build.getRootDir().resolve("Tests/" + project.getName() + "Tests") : project.getProjectDir().resolve("src/test/swift/");
        for (SwiftSourceFile swiftSource : component.getTestFiles()) {
            generateTestSourceFile(testDir, swiftSource, fileGenerator);
        }
        if (!component.getTestFiles().isEmpty()) {
            generateXCTestMain(testDir, component, fileGenerator);
        }
    }

    private void generateSourceFile(Path srcDir, SwiftSourceFile swiftSource, FileGenerator fileGenerator) throws IOException {
        Path sourceFile = srcDir.resolve(swiftSource.getName());
        fileGenerator.generate(sourceFile, printWriter -> {
            printWriter.println("// GENERATED SOURCE FILE");
            printWriter.println();
            if (swiftSource.hasMainFunction()) {
                printWriter.println("import Foundation");
            }
            for (String module : swiftSource.getModules()) {
                printWriter.println("import " + module);
            }
            for (SwiftClass swiftClass : swiftSource.getClasses()) {
                printWriter.println();
                printWriter.println("public class " + swiftClass.getName() + " {");
                printWriter.println("    var visited = false");
                printWriter.println();
                printWriter.println("    public init() { }");
                printWriter.println();
                printWriter.println("    public func doSomething() {");
                printWriter.println("        if (!visited) {");
                printWriter.println("            print(\"visited " + swiftClass.getName() + "\")");
                for (Dependency<SwiftClass> dep : swiftClass.getReferencedClasses()) {
                    SwiftClass targetClass = dep.getTarget();
                    String varName = targetClass.getName().toLowerCase();
                    printWriter.println("            let " + varName + " = " + targetClass.getName() + "()");
                    printWriter.println("            " + varName + ".doSomething()");
                }
                printWriter.println("            visited = true");
                printWriter.println("        }");
                printWriter.println("    }");
                printWriter.println("}");
            }
            if (swiftSource.hasMainFunction()) {
                printWriter.println();
                for (SwiftClass swiftClass : swiftSource.getMainFunctionReferencedClasses()) {
                    String varName = swiftClass.getName().toLowerCase();
                    printWriter.println("let " + varName + " = " + swiftClass.getName() + "()");
                    printWriter.println(varName + ".doSomething()");
                    printWriter.println(varName + ".doSomething()");
                }
            }
            printWriter.println();
        });
    }

    private void generateTestSourceFile(Path srcDir, SwiftSourceFile swiftSource, FileGenerator fileGenerator) throws IOException {
        Path sourceFile = srcDir.resolve(swiftSource.getName());
        fileGenerator.generate(sourceFile, printWriter -> {
            printWriter.println("// GENERATED SOURCE FILE");
            printWriter.println("import XCTest");
            for (String module : swiftSource.getModules()) {
                printWriter.println("import " + module);
            }
            printWriter.println();
            for (SwiftClass swiftClass : swiftSource.getClasses()) {
                String testName = "testOk";
                printWriter.println("class " + swiftClass.getName() + ": XCTestCase {");
                printWriter.println("    func " + testName + "() {");
                XCUnitTest unitTest = swiftClass.role(XCUnitTest.class);
                if (unitTest != null) {
                    String varName = unitTest.getClassUnderTest().getName().toLowerCase();
                    printWriter.println("        let " + varName + " = " + unitTest.getClassUnderTest().getName() + "()");
                    printWriter.println("        " + varName + ".doSomething()");
                }
                printWriter.println("        XCTAssertEqual(1, 1)");
                printWriter.println("    }");
                // Explicit registration is required because the generated entry
                // point uses XCTMain (see generateXCTestMain).
                printWriter.println("    static var allTests = [(\"" + testName + "\", " + testName + ")]");
                printWriter.println("}");
                printWriter.println();
            }
        });
    }

    private void generateXCTestMain(Path testDir, HasSwiftSource component, FileGenerator fileGenerator) throws IOException {
        Path mainFile = testDir.resolve("XCTestMain.swift");
        fileGenerator.generate(mainFile, printWriter -> {
            printWriter.println("// GENERATED SOURCE FILE");
            printWriter.println("import XCTest");
            printWriter.println();
            // The Gradle xctest plugin links a standalone executable, not an
            // .xctest bundle, so we have to supply an entry point ourselves.
            // Without `@main` here the linker fails with "Undefined symbol _main"
            // because relocateMainForTest strips main from the application's
            // object files.
            //
            // XCTMain + testCase(...allTests) is portable: swift-corelibs-xctest
            // exposes them on Linux, and Apple's XCTest provides the same overloads
            // on Darwin specifically so cross-platform entry points can be shared.
            printWriter.println("@main");
            printWriter.println("struct TestRunner {");
            printWriter.println("    static func main() {");
            printWriter.println("        XCTMain([");
            for (SwiftSourceFile testFile : component.getTestFiles()) {
                for (SwiftClass testClass : testFile.getClasses()) {
                    printWriter.println("            testCase(" + testClass.getName() + ".allTests),");
                }
            }
            printWriter.println("        ])");
            printWriter.println("    }");
            printWriter.println("}");
        });
    }
}
