package org.gradle.builds.assemblers;

import org.gradle.builds.generators.GeneratorVersions;
import org.gradle.builds.model.*;

public class JavaModelAssembler extends JvmModelAssembler<JavaApplication, JavaLibrary> {
    public JavaModelAssembler() {
        super(JavaApplication.class, JavaLibrary.class);
    }

    @Override
    protected void library(Settings settings, Project project, JavaLibrary library) {
        project.requires(slfj4);

        JavaClass apiClass = library.getApiClass();

        BuildScript buildScript = project.getBuildScript();
        buildScript.requirePlugin("java");
        addPublishing(project, buildScript);
        addDependencies(project, library, buildScript);
        addJavaVersion(library, buildScript);

        addSource(project, library, apiClass, javaClass -> {
        });
        addTests(project, library);
    }

    @Override
    protected void application(Settings settings, Project project, JavaApplication application) {
        project.requires(slfj4);
        project.requires(slfj4Simple);

        JavaClass mainClass = application.addClass(project.getQualifiedNamespaceFor() + "." + project.getTypeNameFor());
        mainClass.addRole(new AppEntryPoint());

        BuildScript buildScript = project.getBuildScript();
        buildScript.requirePlugin("application");
        addDependencies(project, application, buildScript);
        buildScript.block("application").property("mainClass", mainClass.getName());

        addSource(project, application, mainClass, javaClass -> {
        });
        addTests(project, application);
    }

    private void addPublishing(Project project, BuildScript buildScript) {
        if (project.getPublicationTarget() != null) {
            String group = "org.gradle.example";
            String version = project.getVersion();
            buildScript.property("group", group);
            buildScript.property("version", version);
            if (project.getPublicationTarget().getHttpRepository() != null) {
                buildScript.requirePlugin("maven-publish");
                ScriptBlock publishing = buildScript.block("publishing");
                publishing.block("publications").statement("mavenJava(MavenPublication) { from components.java }");
                ScriptBlock mavenRepo = publishing.block("repositories").block("maven");
                mavenRepo.property("url", project.getPublicationTarget().getHttpRepository().getRootDir().toUri().toString());
                mavenRepo.statement("allowInsecureProtocol = true");
            }
        }
    }

    private void addJavaVersion(JavaLibrary library, BuildScript buildScript) {
        if (library.getTargetJavaVersion() != null) {
            buildScript.block("java").property("sourceCompatibility", library.getTargetJavaVersion());
        }
    }

    private void addDependencies(Project project, HasJavaSource<JavaLibraryApi> component, BuildScript buildScript) {
        // Don't use Android libraries, only Java libraries
        for (Dependency<Library<? extends JavaLibraryApi>> library : project.requiredLibraries(JavaLibraryApi.class)) {
            buildScript.dependsOn("implementation", library.getTarget().getDependency());
            component.uses(library.withTarget(library.getTarget().getApi()));
        }

        buildScript.dependsOnExternal("testImplementation", GeneratorVersions.JUNIT4);
    }
}
