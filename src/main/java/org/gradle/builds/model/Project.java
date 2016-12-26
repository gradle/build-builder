package org.gradle.builds.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class Project {
    private final Set<String> plugins = new LinkedHashSet<>();

    public Set<String> getPlugins() {
        return plugins;
    }

    public void requirePlugin(String id) {
        plugins.add(id);
    }
}
