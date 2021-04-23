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

import com.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.logging.Logging;
import io.spine.tools.mc.java.gradle.check.ErrorProneChecksPlugin;
import io.spine.tools.mc.java.gradle.rejections.RejectionGenPlugin;
import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.mc.java.gradle.annotate.ProtoAnnotatorPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.stream.Stream;

/**
 * Spine Model Compiler Gradle plugin.
 *
 * <p>Applies dependent plugins
 */
public class ModelCompilerPlugin implements Plugin<Project>, Logging {

    private static final String EXTENSION_NAME = "modelCompiler";

    /**
     * Obtains the extension name of the plugin.
     */
    public static String extensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public void apply(Project project) {
        _debug().log("Adding the extension to the project.");
        createExtensionFor(project);

        // Plugins that deal with Protobuf types must depend on `mergeDescriptorSet` and
        // `mergeTestDescriptorSet` tasks to be able to access every declared type
        // in the project classpath.

        Stream.of(new CleaningPlugin(),
                  new DescriptorSetMergerPlugin(),
                  new RejectionGenPlugin(),
                  new ProtoAnnotatorPlugin(),
                  new JavaProtocConfigurationPlugin(),
                  new ErrorProneChecksPlugin())
              .forEach(plugin -> apply(plugin, project));
    }

    /**
     * Creates and registers the extension for the passed project.
     *
     * @return the registered extension
     */
    @VisibleForTesting
    @CanIgnoreReturnValue
    public static Extension createExtensionFor(Project project) {
        Extension extension = new Extension(project, EXTENSION_NAME);
        extension.register();
        return extension;
    }

    private void apply(SpinePlugin plugin, Project project) {
        _debug().log("Applying `%s`.", plugin.getClass().getName());
        plugin.apply(project);
    }
}
