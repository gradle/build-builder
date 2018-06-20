package org.gradle.builds.assemblers;

import org.gradle.builds.model.BuildSettingsBuilder;
import org.gradle.builds.model.BuildTreeBuilder;
import org.gradle.builds.model.Dependency;
import org.gradle.builds.model.PublicationTarget;

public class IncludedBuildAssembler implements BuildTreeAssembler {
    private final ProjectInitializer initializer;
    private final int includedBuilds;

    public IncludedBuildAssembler(ProjectInitializer initializer, int includedBuilds) {
        this.initializer = new EmptyRootProjectInitializer(initializer);
        this.includedBuilds = includedBuilds;
    }

    @Override
    public void attachBuilds(Settings settings, BuildTreeBuilder model) {
        if (includedBuilds > 0) {
            Graph graph = new GraphAssembler().arrange(includedBuilds + 1);
            graph.visit((Graph.Visitor<BuildSettingsBuilder>) (node, dependencies) -> {
                BuildSettingsBuilder build;
                if (node.getLayer() == 0) {
                    build = model.getMainBuild();
                } else {
                    String name = "child" + node.getNameSuffix();
                    String typeName = "Child" + node.getNameSuffix();
                    BuildSettingsBuilder childBuild = model.addBuild(model.getRootDir().resolve(name));
                    childBuild.setDisplayName("included build " + name);
                    childBuild.setRootProjectName(name);
                    childBuild.setSettings(new Settings(3, settings.getSourceFileCount()));
                    childBuild.setProjectInitializer(initializer);
                    childBuild.setTypeNamePrefix(typeName);
                    childBuild.publishAs(new PublicationTarget(null));

                    model.getMainBuild().includeBuild(childBuild);
                    build = childBuild;
                }
                for (Dependency<BuildSettingsBuilder> dependency : dependencies) {
                    build.dependsOn(dependency.getTarget());
                }
                return build;
            });
        }
    }
}
