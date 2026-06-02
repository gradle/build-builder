package org.gradle.builds.assemblers;

import org.gradle.builds.model.*;

import java.util.Collections;

public class AndroidModelAssembler extends JvmModelAssembler<AndroidApplication, AndroidLibrary> {
    /**
     * Default Android Gradle Plugin coordinate emitted into the generated
     * build.gradle. Surfaced as the default value of build-builder's
     * {@code android --version} CLI option in {@link org.gradle.builds.Main}.
     * Update together with {@link #ANDROIDX_LEGACY_SUPPORT_CORE_UTILS} /
     * {@link #ANDROIDX_TEST_RUNNER} when bumping AGP.
     */
    public static final String defaultVersion = "8.10.0";
    private static final String ANDROIDX_LEGACY_SUPPORT_CORE_UTILS = "androidx.legacy:legacy-support-core-utils:1.0.0";
    private static final String ANDROIDX_ANNOTATION = "androidx.annotation:annotation:1.7.0";
    private static final String ANDROIDX_TEST_RUNNER = "androidx.test:runner:1.6.2";
    private static final int COMPILE_SDK = 34;
    private static final int MIN_SDK = 21;
    private static final int TARGET_SDK = 34;
    private static final PublishedLibrary<JavaLibraryApi> supportUtils = new PublishedLibrary<>(
            "support-core-utils",
            new ExternalDependencyDeclaration(ANDROIDX_LEGACY_SUPPORT_CORE_UTILS),
            new JavaLibraryApi("support-core-utils", Collections.singletonList(JavaClassApi.field("androidx.core.app.NavUtils", "PARENT_ACTIVITY"))));
    private final String pluginVersion;

    public AndroidModelAssembler(String pluginVersion) {
        super(AndroidApplication.class, AndroidLibrary.class);
        this.pluginVersion = pluginVersion;
    }

    @Override
    protected void rootProject(Settings settings, Project rootProject) {
        super.rootProject(settings, rootProject);
        BuildScript buildScript = rootProject.getBuildScript();
        buildScript.buildScriptBlock().google();
        buildScript.buildScriptBlock().mavenCentral();
        buildScript.requireOnBuildScriptClasspath("com.android.tools.build:gradle:" + pluginVersion);
        buildScript.allProjects().google();
    }

    @Override
    protected void application(Settings settings, Project project, AndroidApplication androidApplication) {
        project.requires(slfj4);
        project.requires(slfj4Simple);
        project.requires(supportUtils);
        JavaClassApi rClass = JavaClassApi.field(androidApplication.getPackageName() + ".R.string", project.getName().toLowerCase() + "_string");

        JavaClass appActivity = androidApplication.addClass(androidApplication.getPackageName() + "." + project.getTypeNameFor() + "MainActivity");
        appActivity.addRole(new AndroidActivity());
        androidApplication.activity(appActivity);

        BuildScript buildScript = project.getBuildScript();
        buildScript.requirePlugin("com.android.application");
        addDependencies(project, androidApplication, buildScript);
        addApplicationResources(androidApplication);

        ScriptBlock androidBlock = buildScript.block("android");
        androidBlock.property("namespace", androidApplication.getPackageName());
        androidBlock.property("compileSdk", COMPILE_SDK);
        ScriptBlock configBlock = androidBlock.block("defaultConfig");
        configBlock.property("applicationId", androidApplication.getPackageName());
        configBlock.property("minSdk", MIN_SDK);
        configBlock.property("targetSdk", TARGET_SDK);
        configBlock.property("versionCode", 1);
        configBlock.property("versionName", "1.0.0");
        configBlock.property("testInstrumentationRunner", "androidx.test.runner.AndroidJUnitRunner");

        addSourceFiles(project, androidApplication, appActivity, rClass);
        addTests(project, androidApplication);
    }

    @Override
    protected void library(Settings settings, Project project, AndroidLibrary androidLibrary) {
        project.requires(slfj4);
        JavaClassApi rClass = JavaClassApi.field(androidLibrary.getPackageName() + ".R.string", project.getName().toLowerCase() + "_string");

        JavaClass libraryActivity = androidLibrary.getActivity();

        BuildScript buildScript = project.getBuildScript();
        buildScript.requirePlugin("com.android.library");
        addPublishing(project, buildScript);
        addDependencies(project, androidLibrary, buildScript);

        ScriptBlock androidBlock = buildScript.block("android");
        androidBlock.property("namespace", androidLibrary.getPackageName());
        androidBlock.property("compileSdk", COMPILE_SDK);
        ScriptBlock configBlock = androidBlock.block("defaultConfig");
        configBlock.property("minSdk", MIN_SDK);
        configBlock.property("targetSdk", TARGET_SDK);
        configBlock.property("versionCode", 1);
        configBlock.property("versionName", "1.0.0");
        configBlock.property("testInstrumentationRunner", "androidx.test.runner.AndroidJUnitRunner");
        if (project.getPublicationTarget() != null) {
            androidBlock.block("publishing").statement("singleVariant('release') { withSourcesJar() }");
        }

        addSourceFiles(project, androidLibrary, libraryActivity, rClass);
        addTests(project, androidLibrary);
    }

    private void addApplicationResources(AndroidApplication application) {
        String labelResource = "app_label";
        application.stringResource(labelResource, "Test App");
        application.setLabelResource(labelResource);
    }

    private void addPublishing(Project project, BuildScript buildScript) {
        if (project.getPublicationTarget() != null) {
            String group = "org.gradle.example";
            String version = project.getVersion();
            buildScript.property("group", group);
            buildScript.property("version", version);
            if (project.getPublicationTarget().getHttpRepository() != null) {
                buildScript.requirePlugin("maven-publish");
                // The `release` SoftwareComponent only exists after AGP has
                // processed the android { publishing { singleVariant(...) } }
                // block, so the publication wiring must run in afterEvaluate.
                ScriptBlock afterEvaluate = buildScript.block("afterEvaluate");
                ScriptBlock publishing = afterEvaluate.block("publishing");
                publishing.block("publications").statement("mavenJava(MavenPublication) { from components.release }");
                ScriptBlock mavenRepo = publishing.block("repositories").block("maven");
                mavenRepo.property("url", project.getPublicationTarget().getHttpRepository().getRootDir().toUri().toString());
                mavenRepo.statement("allowInsecureProtocol = true");
            }
        }
    }

    private void addDependencies(Project project, AndroidComponent component, BuildScript buildScript) {
        for (Dependency<Library<? extends JvmLibraryApi>> library : project.requiredLibraries(JvmLibraryApi.class)) {
            if (library.isApi()) {
                buildScript.dependsOn("api", library.getTarget().getDependency());
            } else {
                buildScript.dependsOn("implementation", library.getTarget().getDependency());
            }
            component.uses(library.withTarget(library.getTarget().getApi()));
        }
        buildScript.dependsOnExternal("testImplementation", JUNIT_DEPENDENCY);
        // androidx.test:runner pins its own annotation version transitively; do not
        // pin annotation here or we hit strict-constraint conflicts.
        buildScript.dependsOnExternal("androidTestImplementation", ANDROIDX_TEST_RUNNER);
        buildScript.dependsOnExternal("androidTestImplementation", GeneratorVersions.ANDROIDX_TEST_EXT_JUNIT);
    }

    @Override
    protected void addTests(Project project, HasJavaSource application) {
        super.addTests(project, application);
        application.addTest(project.getQualifiedNamespaceFor() + "." + project.getTypeNameFor() + "InstrumentedTest").addRole(new InstrumentedTest());
    }

    private void addSourceFiles(Project project, AndroidComponent androidComponent, JavaClass activity, JavaClassApi rClass) {
        String stringResourceName = project.getName().toLowerCase() + "_string";
        androidComponent.stringResource(stringResourceName, "some-value");

        addSource(project, androidComponent, activity, javaClass -> {
            javaClass.uses(Dependency.implementation(rClass));
        });
    }
}
