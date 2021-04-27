/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.java.gradle;

import com.google.common.collect.ImmutableList;
import io.spine.io.Files2;
import io.spine.tools.java.protoc.Classpath;
import io.spine.tools.java.protoc.SpineProtocConfig;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A {@link SpineProtocConfig} holder.
 */
final class ProtocPluginConfiguration {

    private final SpineProtocConfig config;

    private ProtocPluginConfiguration(SpineProtocConfig config) {
        this.config = config;
    }

    /**
     * Creates a configuration holder for a supplied {@code project}.
     */
    public static ProtocPluginConfiguration forProject(Project project) {
        SpineProtocConfig config = assembleSpineProtocConfig(project);
        return new ProtocPluginConfiguration(config);
    }

    /**
     * Writes the configuration to the file denoted by the supplied {@code configPath}.
     */
    public void writeTo(Path configPath) {
        Files2.ensureFile(configPath);
        try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
            config.writeTo(fos);
        } catch (FileNotFoundException e) {
            throw newIllegalStateException(
                    e,
                    "Unable to create Spine Protoc Plugin configuration file at: `%s`.",
                    configPath);
        } catch (IOException e) {
            throw newIllegalStateException(
                    e,
                    "Unable store Spine Protoc Plugin configuration file at: `%s`.",
                    configPath);
        }
    }

    private static SpineProtocConfig assembleSpineProtocConfig(Project project) {
        Extension extension = Extension.of(project);
        Interfaces interfaces = extension.interfaces;
        Methods methods = extension.methods;
        NestedClasses nestedClasses = extension.nestedClasses;
        Fields fields = extension.fields;
        EntityQueries entityQueries = extension.entityQueries;
        boolean shouldGenerateVBuilders = extension.generateValidatingBuilders;
        boolean shouldGenerateValidation = extension.generateValidation;
        Classpath projectClasspath = projectClasspath(project);

        SpineProtocConfig result = SpineProtocConfig
                .newBuilder()
                .setAddInterfaces(interfaces.asProtocConfig())
                .setAddMethods(methods.asProtocConfig())
                .setAddNestedClasses(nestedClasses.asProtocConfig())
                .setAddFields(fields.asProtocConfig())
                .setAddEntityQueries(entityQueries.asProtocConfig())
                .setSkipValidatingBuilders(!shouldGenerateVBuilders)
                .setGenerateValidation(shouldGenerateValidation)
                .setClasspath(projectClasspath)
                .build();
        return result;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") // Classpath.Builder usage in `forEach`
    private static Classpath projectClasspath(Project project) {
        Classpath.Builder classpath = Classpath.newBuilder();
        Collection<JavaCompile> javaCompileViews = project.getTasks()
                                                          .withType(JavaCompile.class);
        ImmutableList.copyOf(javaCompileViews)
                     .stream()
                     .map(JavaCompile::getClasspath)
                     .map(FileCollection::getFiles)
                     .flatMap(Set::stream)
                     .map(File::getAbsolutePath)
                     .forEach(classpath::addJar);
        return classpath.build();
    }
}