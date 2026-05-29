package org.gradle.builds.model;

import java.nio.file.Path;

public class HttpRepository {
    private final Path rootDir;

    public HttpRepository(Path rootDir) {
        this.rootDir = rootDir;
    }

    public Path getRootDir() {
        return rootDir;
    }
}
