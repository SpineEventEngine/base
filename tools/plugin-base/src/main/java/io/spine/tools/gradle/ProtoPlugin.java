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

package io.spine.tools.gradle;

import io.spine.code.proto.FileSet;
import io.spine.tools.type.FileDescriptorSuperset;
import io.spine.tools.type.MoreKnownTypes;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import java.io.File;
import java.util.function.Supplier;

import static io.spine.tools.gradle.ConfigurationName.RUNTIME;
import static io.spine.tools.gradle.ConfigurationName.TEST_RUNTIME;

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
        Supplier<String> descriptorSetPath = mainDescriptorSetPath(project);
        Configuration configuration = configuration(project, RUNTIME);
        return protoFiles(descriptorSetPath, configuration);
    }

    /**
     * Obtains {@linkplain #protoFiles(Supplier, Configuration) Protobuf} files for the test scope.
     */
    protected final Supplier<FileSet> testProtoFiles(Project project) {
        Supplier<String> descriptorSetPath = testDescriptorSetPath(project);
        Configuration configuration = configuration(project, TEST_RUNTIME);
        return protoFiles(descriptorSetPath, configuration);
    }

    protected abstract Supplier<String> mainDescriptorSetPath(Project project);

    protected abstract Supplier<String> testDescriptorSetPath(Project project);

    /**
     * Obtains all files from the specified descriptor set file and the configuration.
     *
     * <p>Extends {@linkplain MoreKnownTypes known types} with types form collected files.
     *
     * @param descriptorSetPath
     *         the path to the descriptor set file
     * @param configuration
     *         the configuration to scan descriptor set files from
     * @return the collected files
     */
    private static Supplier<FileSet> protoFiles(Supplier<String> descriptorSetPath,
                                                Configuration configuration) {
        return () -> {
            File descriptorSet = new File(descriptorSetPath.get());
            FileDescriptorSuperset superset = new FileDescriptorSuperset();
            configuration.forEach(superset::addFromDependency);
            if (descriptorSet.exists()) {
                superset.addFromDependency(descriptorSet);
            }
            MoreKnownTypes.extendWith(superset.fileSet());
            return superset.fileSet();
        };
    }

    private static Configuration configuration(Project project, ConfigurationName name) {
        return project.getConfigurations()
                      .getByName(name.getValue());
    }
}
