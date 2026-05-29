package org.gradle.builds.generators;

import org.gradle.builds.model.ConfiguredBuild;
import org.gradle.builds.model.ConfiguredProject;
import org.gradle.builds.model.HttpServerImplementation;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

public class SettingsFileGenerator implements Generator<ConfiguredBuild> {
    @Override
    public void generate(ConfiguredBuild build, FileGenerator fileGenerator) throws IOException {
        Path settingsFile = build.getRootDir().resolve("settings.gradle");
        fileGenerator.generate(settingsFile, printWriter -> {
            printWriter.println("// GENERATED SETTINGS SCRIPT");
            printWriter.println("rootProject.name = '" + build.getRootProject().getName() + "'");
            if (!build.getSubprojects().isEmpty()) {
                printWriter.println();
                for (ConfiguredProject project : build.getSubprojects()) {
                    printWriter.println("include '" + project.getName() + "'");
                }
            }

            if (!build.getIncludedBuilds().isEmpty()) {
                printWriter.println();
                for (ConfiguredBuild childBuild : build.getIncludedBuilds()) {
                    printWriter.println("includeBuild '" + build.getRootDir().relativize(childBuild.getRootDir()) + "'");
                }
            }

            if (!build.getSourceBuilds().isEmpty()) {
                printWriter.println();
                printWriter.println("sourceControl.vcsMappings {");
                for (ConfiguredBuild childBuild : build.getSourceBuilds()) {
                    for (ConfiguredProject project : childBuild.getProjects()) {
                        printWriter.println("    withModule('org.gradle.example:" + project.getName() + "') { details ->");
                        printWriter.println("        from(GitVersionControlSpec) {");
                        printWriter.println("            url = uri('" + childBuild.getRootDir().toUri() + "')");
                        printWriter.println("        }");
                        printWriter.println("    }");
                    }
                }
                printWriter.println("}");
            }

            HttpServerImplementation httpRepo = build.getRootProject().component(HttpServerImplementation.class);
            if (httpRepo != null) {
                writeHttpRepoBuildService(printWriter);
            }
        });
    }

    /**
     * Emits a Gradle BuildService that serves the local {@code http-repo/}
     * directory over an ephemeral port. The chosen port is persisted to
     * {@code .gradle/http-repo-port} so configuration-cache hits remain valid
     * across runs; a port collision falls back to a fresh ephemeral port and
     * invalidates the cache through the normal file-input fingerprint.
     */
    private static void writeHttpRepoBuildService(PrintWriter pw) {
        pw.println();
        pw.println("import com.sun.net.httpserver.HttpServer");
        pw.println("import com.sun.net.httpserver.HttpExchange");
        pw.println("import com.sun.net.httpserver.HttpHandler");
        pw.println("import java.net.BindException");
        pw.println("import java.net.InetSocketAddress");
        pw.println("import java.nio.file.Files");
        pw.println("import java.util.concurrent.Executors");
        pw.println("import org.gradle.api.services.BuildService");
        pw.println("import org.gradle.api.services.BuildServiceParameters");
        pw.println("import org.gradle.api.file.DirectoryProperty");
        pw.println("import org.gradle.api.file.RegularFileProperty");
        pw.println();
        pw.println("abstract class HttpRepoServer implements BuildService<HttpRepoServer.Params>, AutoCloseable {");
        pw.println("    interface Params extends BuildServiceParameters {");
        pw.println("        DirectoryProperty getRepoDir()");
        pw.println("        RegularFileProperty getPortFile()");
        pw.println("    }");
        pw.println();
        pw.println("    private final HttpServer server");
        pw.println("    final int port");
        pw.println();
        pw.println("    HttpRepoServer() {");
        pw.println("        def repoDir = parameters.repoDir.get().asFile");
        pw.println("        def portFile = parameters.portFile.get().asFile");
        pw.println("        def preferred = (portFile.isFile() && portFile.text.trim().isInteger())");
        pw.println("                ? portFile.text.trim() as int");
        pw.println("                : 0");
        pw.println("        server = tryBind(preferred)");
        pw.println("        port = server.address.port");
        pw.println("        portFile.parentFile.mkdirs()");
        pw.println("        portFile.text = port as String");
        pw.println("        server.executor = Executors.newCachedThreadPool()");
        pw.println("        server.createContext('/', { HttpExchange ex ->");
        pw.println("            def rel = ex.requestURI.path");
        pw.println("            def f = new File(repoDir, rel.startsWith('/') ? rel.substring(1) : rel)");
        pw.println("            if (!f.isFile()) {");
        pw.println("                ex.sendResponseHeaders(404, -1)");
        pw.println("            } else if (ex.requestMethod != 'GET') {");
        pw.println("                ex.sendResponseHeaders(200, -1)");
        pw.println("            } else {");
        pw.println("                ex.sendResponseHeaders(200, f.length())");
        pw.println("                ex.responseBody.withCloseable { out -> Files.copy(f.toPath(), out) }");
        pw.println("            }");
        pw.println("            ex.close()");
        pw.println("        } as HttpHandler)");
        pw.println("        server.start()");
        pw.println("    }");
        pw.println();
        pw.println("    private static HttpServer tryBind(int p) {");
        pw.println("        try { return HttpServer.create(new InetSocketAddress(p), 20) }");
        pw.println("        catch (BindException ignored) { return HttpServer.create(new InetSocketAddress(0), 20) }");
        pw.println("    }");
        pw.println();
        pw.println("    @Override void close() { server.stop(0) }");
        pw.println("}");
        pw.println();
        pw.println("def httpRepoPortFile = layout.rootDirectory.file('.gradle/http-repo-port')");
        pw.println("def httpRepoService = gradle.sharedServices.registerIfAbsent('httpRepo', HttpRepoServer) {");
        pw.println("    parameters.repoDir = layout.rootDirectory.dir('http-repo')");
        pw.println("    parameters.portFile = httpRepoPortFile");
        pw.println("}");
        pw.println("httpRepoService.get()  // force start so the port file exists before projects evaluate");
        pw.println("gradle.ext.httpRepoPort = httpRepoPortFile.asFile.text.trim() as int");
    }
}
