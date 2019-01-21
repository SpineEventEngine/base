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
package io.spine.tools.gradle.compiler;

import io.spine.logging.Logging;
import io.spine.tools.gradle.SpinePlugin;
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

    @SuppressWarnings("OverlyCoupledMethod") // OK as we need to launch all sub-plugins.
    @Override
    public void apply(Project project) {
        _debug("Adding the extension to the project.");
        project.getExtensions()
               .create(extensionName(), Extension.class);

        // Plugins that deal with Protobuf types must depend on `MERGE_DESCRIPTOR_SET` and
        // `MERGE_TEST_DESCRIPTOR_SET` tasks to be able to access every declared type
        // in the project classpath.

        Stream.of(new CleaningPlugin(),
                  new EnrichmentLookupPlugin(),
                  new RejectionGenPlugin(),
                  new ValidatingBuilderGenPlugin(),
                  new ProtoAnnotatorPlugin(),
                  new ValidationRulesLookupPlugin(),
//                  new ProtocPluginImporter(),
                  new ProtocConfigurationPlugin(),
                  new DescriptorSetMergerPlugin(),
                  new ErrorProneChecksPlugin())
              .forEach(plugin -> apply(plugin, project));
    }

    private void apply(SpinePlugin plugin, Project project) {
        _debug("Applying {}", plugin.getClass()
                                         .getName());
        plugin.apply(project);
    }
}
