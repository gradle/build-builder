package org.gradle.builds.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class BlockWithRepositories extends Scope {
    private final Set<ScriptBlock> repositories = new LinkedHashSet<>();

    public Set<ScriptBlock> getRepositories() {
        return repositories;
    }

    public void mavenCentral() {
        repositories.add(new ScriptBlock("mavenCentral"));
    }

    public void google() {
        repositories.add(new ScriptBlock("google"));
    }

    public void mavenLocal() {
        repositories.add(new ScriptBlock("mavenCentral"));
    }

    public void maven(HttpRepository repo) {
        ScriptBlock block = new ScriptBlock("maven");
        // The repo is served by an embedded HttpServer started as a Gradle
        // BuildService from settings.gradle; the assigned port is exposed via
        // gradle.ext.httpRepoPort. Parallel test forks each get their own.
        block.property("url", new Scope.Code(
                "\"http://localhost:${gradle.httpRepoPort}/\""));
        // Gradle 7+ requires explicit opt-in for plain http:// repositories.
        block.statement("allowInsecureProtocol = true");
        repositories.add(block);
    }
}
