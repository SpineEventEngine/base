/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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
package org.spine3.gradle.reflections;

import com.google.common.io.Files;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.serializers.Serializer;
import org.reflections.serializers.XmlSerializer;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.gradle.SpinePlugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static java.io.File.separatorChar;
import static org.spine3.gradle.TaskName.BUILD;
import static org.spine3.gradle.TaskName.CLASSES;
import static org.spine3.gradle.TaskName.SCAN_CLASS_PATH;
import static org.spine3.gradle.reflections.Extension.REFLECTIONS_PLUGIN_EXTENSION;

/**
 * Gradle port for Maven Reflections Plugin.
 *
 * <p>Uses reflections embedded scanners to build it's serialized config. This
 * serialized config is required for Reflections framework to run.
 *
 * <p>Corresponding Maven plugin does just the same.
 *
 * @author Alex Tymchenko
 */
public class ReflectionsPlugin extends SpinePlugin {

    /**
     * Applied to project.
     *
     * <p>The plugin runs after `:classes` task and before `:processResources`.
     */
    @Override
    public void apply(final Project project) {
        log().debug("Applying the Reflections plugin");
        project.getExtensions()
               .create(REFLECTIONS_PLUGIN_EXTENSION, Extension.class);

        final Action<Task> scanClassPathAction = new Action<Task>() {
            @Override
            public void execute(Task task) {
                scanClassPath(project);
            }
        };
        final GradleTask task = newTask(SCAN_CLASS_PATH, scanClassPathAction).insertAfterTask(CLASSES)
                                                                             .insertBeforeTask(BUILD)
                                                                             .applyNowTo(project);

        log().debug("Reflection Gradle plugin initialized with the Gradle task: {}", task);
    }

    private static void scanClassPath(Project project) {
        log().debug("Scanning the classpath");

        final String outputDirPath = project.getProjectDir() + "/build";
        final File outputDir = new File(outputDirPath);
        ensureFolderCreated(outputDir);

        final String targetDirPath = Extension.getTargetDir(project);
        final File reflectionsOutputDir = new File(targetDirPath);
        ensureFolderCreated(reflectionsOutputDir);

        final ConfigurationBuilder config = new ConfigurationBuilder();
        config.setUrls(toUrls(outputDir));
        config.setScanners(new SubTypesScanner(), new TypeAnnotationsScanner());

        final Serializer serializerInstance = new XmlSerializer();
        config.setSerializer(serializerInstance);

        final Reflections reflections = new Reflections(config);
        final String reflectionsOutputFilePath =
                targetDirPath + separatorChar + project.getName() + "-reflections.xml";
        reflections.save(reflectionsOutputFilePath);
    }

    private static Set<URL> toUrls(File outputDir) {
        // because they are file URIs, they will not cause any network-related issues.
        @SuppressWarnings("CollectionContainsUrl")
        final Set<URL> urls = new HashSet<>();
        try {
            urls.add(outputDir.toURI()
                              .toURL());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Cannot parse an output directory: " + outputDir.getAbsolutePath(), e);
        }
        return urls;
    }

    private static void ensureFolderCreated(File folder) {
        try {
            Files.createParentDirs(folder);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create a folder: " + folder.getAbsolutePath(), e);
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ReflectionsPlugin.class);
    }
}
