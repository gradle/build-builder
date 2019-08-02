package org.gradle.builds.model;

import org.gradle.builds.assemblers.ComposableProjectInitializer;
import org.gradle.builds.assemblers.Settings;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Allows the settings for a build to be configured.
 */
public class DefaultBuildSettingsBuilder implements BuildSettingsBuilder {
    private final Path rootDir;
    private String displayName;
    private String rootProjectName;
    private Settings settings;
    private ComposableProjectInitializer projectInitializer = new ComposableProjectInitializer();
    private PublicationTarget publicationTarget;
    private String typeNamePrefix = "";
    private String version = "1.0.0";
    private final List<BuildSettingsBuilder> dependsOn = new ArrayList<>();
    private final List<BuildSettingsBuilder> includedBuilds = new ArrayList<>();
    private final List<BuildSettingsBuilder> sourceBuilds = new ArrayList<>();

    public DefaultBuildSettingsBuilder(Path rootDir) {
        this.rootDir = rootDir;
    }

    public BuildProjectTreeBuilder toModel(Function<BuildSettingsBuilder, BuildProjectTreeBuilder> otherBuildLookup) {
        assertNotNull("displayName", displayName);
        assertNotNull("rootProjectName", rootProjectName);
        assertNotNull("settings", settings);
        assertNotNull("projectInitializer", projectInitializer);

        List<BuildProjectTreeBuilder> dependsOnBuilds = dependsOn.stream().map(otherBuildLookup).collect(Collectors.toList());
        List<BuildProjectTreeBuilder> includedBuilds = this.includedBuilds.stream().map(otherBuildLookup).collect(Collectors.toList());
        List<BuildProjectTreeBuilder> sourceBuilds = this.sourceBuilds.stream().map(otherBuildLookup).collect(Collectors.toList());
        return new DefaultBuildProjectTreeBuilder(rootDir, displayName, rootProjectName, settings, publicationTarget, typeNamePrefix, projectInitializer, version, dependsOnBuilds, includedBuilds, sourceBuilds);
    }

    private void assertNotNull(String name, @Nullable Object value) {
        if (value == null) {
            throw new IllegalStateException(String.format("No value specified for property '%s'", name));
        }
    }

    @Override
    public Path getRootDir() {
        return rootDir;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setRootProjectName(String rootProjectName) {
        this.rootProjectName = rootProjectName;
    }

    @Override
    public String getRootProjectName() {
        return rootProjectName;
    }

    @Override
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public ComposableProjectInitializer getProjectInitializer() {
        return projectInitializer;
    }

    @Override
    public void setTypeNamePrefix(String typeNamePrefix) {
        this.typeNamePrefix = typeNamePrefix;
    }

    @Override
    public String getTypeNamePrefix() {
        return typeNamePrefix;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void publishAs(PublicationTarget publicationTarget) {
        this.publicationTarget = publicationTarget;
    }

    @Override
    public void sourceDependency(BuildSettingsBuilder childBuild) {
        this.sourceBuilds.add(childBuild);
    }

    @Override
    public void dependsOn(BuildSettingsBuilder childBuild) {
        this.dependsOn.add(childBuild);
    }

    @Override
    public void includeBuild(BuildSettingsBuilder childBuild) {
        this.includedBuilds.add(childBuild);
    }
}
