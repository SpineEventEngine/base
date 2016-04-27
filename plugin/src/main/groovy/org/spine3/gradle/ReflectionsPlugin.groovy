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
 * <p>Uses reflections embedded scanners to build it's serialized config.
 */
class ReflectionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project target) {
        final Task scanClassPath = target.task("scanClassPath") {
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
        target.getTasks().getByPath("build").dependsOn(scanClassPath)
    }
}
