/*
 * Copyright 2019, TeamDev. All rights reserved.
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
package io.spine.tools.reflections;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import static io.spine.tools.gradle.TaskName.BUILD;
import static io.spine.tools.gradle.TaskName.CLASSES;
import static io.spine.tools.gradle.TaskName.SCAN_CLASS_PATH;
import static io.spine.tools.reflections.Extension.REFLECTIONS_PLUGIN_EXTENSION;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.io.File.separatorChar;

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
    public void apply(Project project) {
        Logger log = log();
        log.debug("Applying the Reflections plugin");
        project.getExtensions()
               .create(REFLECTIONS_PLUGIN_EXTENSION, Extension.class);

        Action<Task> scanClassPathAction = task -> scanClassPath(project);
        GradleTask task = newTask(SCAN_CLASS_PATH, scanClassPathAction)
                .insertAfterTask(CLASSES)
                .insertBeforeTask(BUILD)
                .applyNowTo(project);

        log.debug("Reflection Gradle plugin initialized with the Gradle task: {}", task);
    }

    private void scanClassPath(Project project) {
        log().debug("Scanning the classpath");

        String outputDirPath = project.getProjectDir() + "/build";
        File outputDir = new File(outputDirPath);
        ensureFolderCreated(outputDir);

        String targetDirPath = Extension.getTargetDir(project);
        File reflectionsOutputDir = new File(targetDirPath);
        ensureFolderCreated(reflectionsOutputDir);

        ConfigurationBuilder config = new ConfigurationBuilder();
        config.setUrls(toUrls(outputDir));
        config.setScanners(new SubTypesScanner(), new TypeAnnotationsScanner());

        Serializer serializerInstance = new XmlSerializer();
        config.setSerializer(serializerInstance);

        Reflections reflections = new Reflections(config);
        String reflectionsOutputFilePath =
                targetDirPath + separatorChar + project.getName() + "-reflections.xml";
        reflections.save(reflectionsOutputFilePath);
    }

    @SuppressWarnings({"CollectionContainsUrl", "URLEqualsHashCode"})
    // because they are file URIs, they will not cause any network-related issues.
    private static Set<URL> toUrls(File outputDir) {
        ImmutableSet<URL> urls;
        try {
            urls = ImmutableSet.of(outputDir.toURI()
                                            .toURL());
        } catch (MalformedURLException e) {
            throw newIllegalArgumentException(
                    e, "Cannot parse an output directory: %s", outputDir.getAbsolutePath()
            );
        }
        return urls;
    }

    private static void ensureFolderCreated(File folder) {
        try {
            Files.createParentDirs(folder);
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Cannot create a folder: %s", folder.getAbsolutePath()
            );
        }
    }
}
