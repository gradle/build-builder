package org.gradle.builds.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Marks a build whose consumer projects resolve from an http-served local maven
 * repository. The generated settings script starts an embedded HttpServer as a
 * Gradle BuildService; library builds listed here are published into the repo
 * via {@code GradleBuild} tasks attached to this build's root project.
 */
public class HttpServerImplementation implements Component, HasHeapRequirements {
    private final Path repoDir;
    private final List<Path> sourceBuilds = new ArrayList<>();

    public HttpServerImplementation(HttpRepository httpRepository) {
        this.repoDir = httpRepository.getRootDir();
    }

    public List<Path> getSourceBuilds() {
        return sourceBuilds;
    }

    public void addSourceBuild(Path sourceDir) {
        sourceBuilds.add(sourceDir);
    }

    public Path getRepoDir() {
        return repoDir;
    }

    @Override
    public int getMinHeapMegabytes() {
        // This daemon runs the embedded HttpServer AND spawns nested
        // GradleBuild tasks that publish the library builds (which may be
        // Android). Outer + inner daemons together need headroom.
        return 2048;
    }
}
