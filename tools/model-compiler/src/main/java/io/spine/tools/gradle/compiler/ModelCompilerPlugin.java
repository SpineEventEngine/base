/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

/**
 * Spine Model Compiler Gradle plugin.
 *
 * <p>Applies dependent plugins
 *
 * @author Alexander Litus
 * @author Mikhail Mikhaylov
 */
public class ModelCompilerPlugin implements Plugin<Project> {

    private static final String EXTENSION_NAME = "modelCompiler";

    /**
     * Obtains the extension name of the plugin.
     */
    public static String extensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public void apply(Project project) {
        log().debug("Adding the extension to the project.");
        project.getExtensions()
               .create(extensionName(), Extension.class);

        Stream.of(new CleaningPlugin(),
                  new EnrichmentLookupPlugin(),
                  new RejectionGenPlugin(),
                  new ValidatingBuilderGenPlugin(),
                  new ProtoAnnotatorPlugin(),
                  new ValidationRulesLookupPlugin(),
                  new ProtocPluginImporter(),
                  new CompileConfiguration())
              .forEach(plugin -> apply(plugin, project));
    }

    private static void apply(SpinePlugin plugin, Project project) {
        log().debug("Applying {}", plugin.getClass()
                                         .getName());
        plugin.apply(project);
    }

    private static Logger log() {
        return LoggerSingleton.INSTANCE.logger;
    }

    private enum LoggerSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger logger = LoggerFactory.getLogger(ModelCompilerPlugin.class);
    }
}
