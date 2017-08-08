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
package io.spine.gradle.compiler;

import io.spine.gradle.compiler.annotation.ProtoAnnotatorPlugin;
import io.spine.gradle.compiler.cleaning.CleaningPlugin;
import io.spine.gradle.compiler.lookup.enrichment.EnrichmentLookupPlugin;
import io.spine.gradle.compiler.lookup.proto.ProtoToJavaMapperPlugin;
import io.spine.gradle.compiler.lookup.valrule.ValidationRulesLookupPlugin;
import io.spine.gradle.compiler.protoc.ProtocPluginImporter;
import io.spine.gradle.compiler.rejection.RejectionGenPlugin;
import io.spine.gradle.compiler.validate.ValidatingBuilderGenPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander Litus
 * @author Mikhail Mikhaylov
 */
public class ModelCompilerPlugin implements Plugin<Project> {

    static final String SPINE_MODEL_COMPILER_EXTENSION_NAME = "modelCompiler";

    @Override
    public void apply(Project project) {
        log().debug("Adding the extension to the project.?????????");
        project.getExtensions()
               .create(SPINE_MODEL_COMPILER_EXTENSION_NAME, Extension.class);

        log().debug("Applying Spine cleaning plugin.");
        new CleaningPlugin().apply(project);


        log().debug("Applying Spine proto-to-java mapper plugin.");
        new ProtoToJavaMapperPlugin().apply(project);

        log().debug("Applying Spine enrichment lookup plugin.");
        new EnrichmentLookupPlugin().apply(project);

        log().debug("Applying Spine rejection generation plugin.");
        new RejectionGenPlugin().apply(project);

        log().debug("Applying Spine validating builder generation plugin.");
        new ValidatingBuilderGenPlugin().apply(project);

        log().debug("Applying Spine proto annotator plugin.");
        new ProtoAnnotatorPlugin().apply(project);

        log().debug("Applying Spine validation rules lookup plugin.");
        new ValidationRulesLookupPlugin().apply(project);

        log().debug("Applying Spine protoc-plugin importer plugin.");
        new ProtocPluginImporter().apply(project);
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
