package org.gradle.builds.assemblers;

import org.gradle.builds.model.AndroidApplication;
import org.gradle.builds.model.AndroidLibrary;
import org.gradle.builds.model.JavaLibrary;
import org.gradle.builds.model.Project;

public class AndroidBuildProjectInitializer extends ProjectInitializer {
    private final boolean includeJavaLibraries;

    public AndroidBuildProjectInitializer(boolean includeJavaLibraries) {
        this.includeJavaLibraries = includeJavaLibraries;
    }

    @Override
    public void initRootProject(Project project) {
        project.addComponent(new AndroidApplication(project));
    }

    @Override
    public void initLibraryProject(Project project) {
        project.addComponent(new AndroidLibrary(project));
    }

    @Override
    public void initAlternateLibraryProject(Project project) {
        if (includeJavaLibraries) {
            JavaLibrary javaLibrary = new JavaLibrary(project);
            // Source/target option 7 was removed by javac. Modern AGP requires
            // Java 11+ for its own classpath anyway, so emit 11 for the
            // Java libraries bundled into the Android build.
            javaLibrary.setTargetJavaVersion("11");
            project.addComponent(javaLibrary);
        } else {
            initLibraryProject(project);
        }
    }
}
