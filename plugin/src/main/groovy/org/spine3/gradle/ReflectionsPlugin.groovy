package org.spine3.gradle

import org.gradle.api.Task
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.serializers.XmlSerializer
import org.reflections.util.ConfigurationBuilder

/**
 * Gradle port for Maven Reflections Plugin.
 *
 * <p>Uses reflections embedded scanners to build it's serialized config. This
 * serialized config is required for Reflections framework to run.
 *
 * <p>Corresponding Maven plugin does just the same.
 */
class ReflectionsPlugin implements Plugin<Project> {

    /**
     * Applied to project.
     *
     * <p>The plugin runs after :classes task and before :build.
     */
    @Override
    void apply(Project target) {
        final Task scanClassPath = target.task("scanClassPath") {
            // TODO:2016-07-11:mikhail.mikhaylov: @alexander.litus I would suggest finalizing variables and using semicolons
            def outputDir = "${target.projectDir}/build"
            def reflectionsOutputDir = "${target.projectDir}/src/generated/resources/META-INF/reflections"
            def reflectionsOutputFilePath = "$reflectionsOutputDir/${project.name}-reflections.xml"

            def outputFile = new File(outputDir)
            def reflectionsOutputDirFile = new File(reflectionsOutputDir)

            outputFile.mkdirs()
            reflectionsOutputDirFile.mkdirs()

            def config = new ConfigurationBuilder()

            final def urls = new HashSet<URL>()
            urls.add(outputFile.toURI().toURL())

            config.setUrls(urls)

            config.setScanners(new SubTypesScanner(), new TypeAnnotationsScanner());

            def serializerInstance = new XmlSerializer();
            config.setSerializer(serializerInstance);

            def reflections = new Reflections(config);

            reflections.save(reflectionsOutputFilePath);
        }

        scanClassPath.dependsOn("classes")
        // TODO:2016-07-11:mikhail.mikhaylov: @alexander.litus I would suggest changing :build dependency to :processResources
        target.getTasks().getByPath("build").dependsOn(scanClassPath)
    }
}
