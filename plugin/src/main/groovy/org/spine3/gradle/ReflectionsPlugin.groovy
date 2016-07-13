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
