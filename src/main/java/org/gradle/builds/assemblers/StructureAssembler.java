package org.gradle.builds.assemblers;

import org.gradle.builds.model.Application;
import org.gradle.builds.model.Build;
import org.gradle.builds.model.Library;
import org.gradle.builds.model.Project;

public class StructureAssembler {
    private final ProjectDecorator decorator;

    public StructureAssembler(ProjectDecorator decorator) {
        this.decorator = decorator;
    }

    public void arrangeClasses(Build build) {
        Graph classGraph = new Graph();
        Settings settings = build.getSettings();
        new GraphAssembler().arrange(settings.getSourceFileCount(), classGraph);
        System.out.println("* Arranging source files in " + classGraph.getLayers().size() + " layers per project.");
        for (Project project : build.getProjects()) {
            project.setClassGraph(classGraph);
        }
    }

    public void arrangeProjects(Build build) {
        Graph projectGraph = new Graph();
        Settings settings = build.getSettings();
        new GraphAssembler().arrange(settings.getProjectCount(), projectGraph);
        System.out.println("* Arranging projects in " + projectGraph.getLayers().size() + " layers.");

        projectGraph.visit((Graph.Visitor<Project>) (layer, item, lastLayer, dependencies) -> {
            Project project;
            if (layer == 0) {
                project = build.getRootProject();
                if (build.getRootProjectType() != null) {
                    decorator.apply(build.getRootProjectType(), project);
                }
            } else {
                String name;
                if (lastLayer) {
                    name = "core" + (item + 1);
                } else {
                    name = "lib" + layer + "_" + (item + 1);
                }
                project = build.addProject(name);
                if (lastLayer && item == 0) {
                    project.setMayUseOtherLanguage(true);
                }
                decorator.apply(Library.class, project);
            }
            for (Project dep : dependencies) {
                project.dependsOn(dep);
            }
            return project;
        });
    }
}
