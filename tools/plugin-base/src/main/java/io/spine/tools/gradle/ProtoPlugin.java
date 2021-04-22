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

package io.spine.tools.gradle;

import io.spine.tools.code.proto.FileSet;
import io.spine.tools.type.FileDescriptorSuperset;
import io.spine.tools.type.MergedDescriptorSet;
import io.spine.tools.type.MoreKnownTypes;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import java.io.File;
import java.util.function.Supplier;

import static io.spine.tools.gradle.ConfigurationName.runtimeClasspath;
import static io.spine.tools.gradle.ConfigurationName.testRuntimeClasspath;

/**
 * A plugin performing code-generation based on a {@code .proto} files.
 *
 * @implNote This class uses {@code Supplier}s instead of direct values because at the time
 *           of creation Gradle project is not fully evaluated, and the values
 *           are not yet defined.
 */
public abstract class ProtoPlugin extends SpinePlugin {

    /**
     * Obtains {@linkplain #protoFiles(Supplier, Configuration) Protobuf} files for the main scope.
     */
    protected final Supplier<FileSet> mainProtoFiles(Project project) {
        Supplier<File> descriptorSet = mainDescriptorFile(project);
        Configuration configuration = configuration(project, runtimeClasspath);
        return protoFiles(descriptorSet, configuration);
    }

    /**
     * Obtains {@linkplain #protoFiles(Supplier, Configuration) Protobuf} files for the test scope.
     */
    protected final Supplier<FileSet> testProtoFiles(Project project) {
        Supplier<File> descriptorSet = testDescriptorFile(project);
        Configuration configuration = configuration(project, testRuntimeClasspath);
        return protoFiles(descriptorSet, configuration);
    }

    /**
     * Obtains the descriptor set file for the main scope.
     */
    protected abstract Supplier<File> mainDescriptorFile(Project project);

    /**
     * Obtains the descriptor set file for the test scope.
     */
    protected abstract Supplier<File> testDescriptorFile(Project project);

    /**
     * Obtains all files from the specified descriptor set file and the configuration.
     *
     * <p>Extends {@linkplain MoreKnownTypes known types} with types form collected files.
     *
     * @param descriptorSet
     *         the path to the descriptor set file
     * @param configuration
     *         the configuration to scan descriptor set files from
     * @return the collected files
     */
    private static Supplier<FileSet> protoFiles(Supplier<File> descriptorSet,
                                                Configuration configuration) {
        return () -> {
            FileDescriptorSuperset superset = new FileDescriptorSuperset();
            configuration.forEach(superset::addFromDependency);
            File suppliedDescriptorSet = descriptorSet.get();
            if (suppliedDescriptorSet.exists()) {
                superset.addFromDependency(suppliedDescriptorSet);
            }
            MergedDescriptorSet mergedSet = superset.merge();
            mergedSet.loadIntoKnownTypes();
            return mergedSet.fileSet();
        };
    }

    private static Configuration configuration(Project project, ConfigurationName name) {
        return project.getConfigurations()
                      .getByName(name.value());
    }
}
