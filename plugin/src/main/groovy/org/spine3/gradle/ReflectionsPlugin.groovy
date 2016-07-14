/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spine3.gradle
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.serializers.Serializer
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
     * <p>The plugin runs after `:classes` task and before `:processResources`.
     */
    @Override
    void apply(Project project) {
        final Task scanClassPath = project.task("scanClassPath") {
            scanClassPath(project)
        }
        scanClassPath.dependsOn("classes")
        project.getTasks().getByPath("processResources").dependsOn(scanClassPath)
    }

    private static void scanClassPath(Project project) {
        final GString outputDir = "${project.projectDir}/build"
        final File outputFile = new File(outputDir)
        outputFile.mkdirs()

        final GString reflectionsOutputDir = "${project.projectDir}/src/generated/resources/META-INF/reflections"
        final File reflectionsOutputDirFile = new File(reflectionsOutputDir)
        reflectionsOutputDirFile.mkdirs()

        final ConfigurationBuilder config = new ConfigurationBuilder()
        final Set urls = new HashSet<URL>()
        urls.add(outputFile.toURI().toURL())
        config.setUrls(urls)

        config.setScanners(new SubTypesScanner(), new TypeAnnotationsScanner())

        final Serializer serializerInstance = new XmlSerializer()
        config.setSerializer(serializerInstance)

        final Reflections reflections = new Reflections(config)
        final GString reflectionsOutputFilePath = "$reflectionsOutputDir/${project.name}-reflections.xml"
        reflections.save(reflectionsOutputFilePath)
    }
}
