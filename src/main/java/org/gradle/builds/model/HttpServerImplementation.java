package org.gradle.builds.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HttpServerImplementation implements Component, HasHeapRequirements {
    private final Path rootDir;
    private final int port;
    private final List<Path> sourceBuilds = new ArrayList<>();

    public HttpServerImplementation(HttpRepository httpRepository) {
        this.rootDir = httpRepository.getRootDir();
        this.port = httpRepository.getHttpPort();
    }

    public List<Path> getSourceBuilds() {
        return sourceBuilds;
    }

    public void addSourceBuild(Path sourceDir) {
        sourceBuilds.add(sourceDir);
    }

    public Path getRootDir() {
        return rootDir;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int getMinHeapMegabytes() {
        // The HTTP-repo server build invokes nested GradleBuild tasks that
        // publish the library builds (which may be Android). With the outer
        // and inner Gradle daemons both in memory we need a generous cap or
        // the daemon GC-thrashes itself to death.
        return 2048;
    }
}
