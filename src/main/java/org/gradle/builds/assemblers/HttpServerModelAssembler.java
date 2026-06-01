package org.gradle.builds.assemblers;

import org.gradle.builds.model.*;

import java.nio.file.Path;

/**
 * Wires the GradleBuild publish tasks for each external library build into
 * the consuming (main) build's root project, and forces the standard entry
 * tasks (build/assemble/installDist/etc.) to depend on them so the http-repo
 * is populated before the embedded server starts answering resolution
 * requests.
 */
public class HttpServerModelAssembler extends ComponentSpecificProjectConfigurer<HttpServerImplementation> {
    public HttpServerModelAssembler() {
        super(HttpServerImplementation.class);
    }

    @Override
    public void configure(Settings settings, Project project, HttpServerImplementation component) {
        BuildScript buildScript = project.getBuildScript();
        for (int i = 0; i < component.getSourceBuilds().size(); i++) {
            Path path = component.getSourceBuilds().get(i);
            String publishTaskName = "publishExternalsV" + (i + 1);
            ScriptBlock taskBlock = buildScript.block("task " + publishTaskName + "(type: GradleBuild)");
            taskBlock.property("dir", new Scope.Code("file('" + path.toUri() + "')"));
            taskBlock.property("tasks", new Scope.Code("['publish']"));
        }
        // Aggregator the consumer (or a test) runs first to populate the
        // local http-repo. Several language plugins (Cpp's `cpp-application`,
        // AGP's `com.android.application`) resolve runtime/compile
        // configurations during task-graph realization to decide what link or
        // assemble subtasks even exist — that's earlier than any in-graph
        // task ordering can affect, so the publishing has to happen in a
        // prior Gradle invocation. The repo persists, so subsequent runs of
        // the consumer entry point resolve straight from disk.
        buildScript.statement("task publishHttpRepo { dependsOn tasks.withType(GradleBuild) }");
    }
}
