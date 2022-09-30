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

    public void maven(HttpRepository repo) {
        ScriptBlock block = new ScriptBlock("maven");
        block.property("url", repo.getUri().toString());
        repositories.add(block);
    }
}
