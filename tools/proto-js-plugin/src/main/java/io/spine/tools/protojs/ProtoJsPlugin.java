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

package io.spine.tools.protojs;

import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.nio.file.Path;

import static io.spine.tools.gradle.TaskName.COMPILE_PROTO_TO_JS;
import static io.spine.tools.gradle.TaskName.COPY_MODULE_SOURCES;
import static io.spine.tools.gradle.TaskName.GENERATE_FROM_JSON;
import static io.spine.tools.protojs.ProtoFromJsonWriter.createFor;
import static io.spine.tools.protojs.files.ProjectFiles.mainDescriptorSetFile;
import static io.spine.tools.protojs.files.ProjectFiles.mainProtoJsLocation;
import static io.spine.tools.protojs.files.ProjectFiles.testDescriptorSetFile;
import static io.spine.tools.protojs.files.ProjectFiles.testProtoJsLocation;

/**
 * The Gradle plugin which generates the code to parse JavaScript proto definitions from the JSON
 * format.
 *
 * <p>To run the plugin, add the {@code "io.spine.tools:spine-proto-js-plugin:$spineBaseVersion"}
 * to the {@code classpath} configuration and add
 * {@code apply plugin: 'io.spine.tools.proto-js-plugin'} to the module generating JS protos.
 *
 * <p>The tool will create the {@code fromJson(json)} method for every JS message definition found
 * among the {@linkplain io.spine.code.proto.FileSet known types}.
 *
 * <p>This plugin currently relies on the set of the hard-coded Gradle settings which have to be
 * set to the required values in a project willing to use the plugin. These settings are:
 * <ol>
 *     <li>The JS proto definitions path set to {@code ${projectDir}/proto/${sourceSet}/js"};
 *
 *     <li>descriptor set file stored under the
 *         {@code "${projectDir}/build/descriptors/${task.sourceSet.name}/known_types.desc"};
 *
 *     <li>CommonJS import style for all generated proto definitions:
 *         {@code js {option "import_style=commonjs"}};
 *
 *     <li>{@code compileProtoToJs} and {@code copyModuleSources} tasks available in the project.
 * </ol>
 *
 * <p>In general, it is how the <a href="https://github.com/SpineEventEngine/web">Spine Web</a>
 * builds its Protobuf definitions to JS and the plugin relies on this behaviour.
 *
 * <p>The {@code build.gradle} file located under the {@code test/resources} folder of this module
 * contains the configuration of the project to which the plugin can be successfully applied. It
 * can thus be used as an example.
 *
 * @author Dmytro Kuzmin
 */
public class ProtoJsPlugin extends SpinePlugin {

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // Method called to apply task.
    @Override
    public void apply(Project project) {
        Action<Task> task = newAction(project);
        newTask(GENERATE_FROM_JSON, task)
                .insertAfterTask(COMPILE_PROTO_TO_JS)
                .insertBeforeTask(COPY_MODULE_SOURCES)
                .applyNowTo(project);
    }

    /**
     * Creates an {@code Action} to generate the {@code fromJson} method for both main and test
     * proto definitions.
     *
     * <p>The paths to the JS proto definitions location, as well as to the descriptor set file,
     * are currently hard-coded.
     *
     * <p>See {@link io.spine.tools.protojs.files.ProjectFiles} for the expected configuration.
     */
    private static Action<Task> newAction(Project project) {
        return task -> generateFromJsonForProto(project);
    }

    private static void generateFromJsonForProto(Project project) {
        generateForMain(project);
        generateForTest(project);
    }

    private static void generateForMain(Project project) {
        Path protoJsLocation = mainProtoJsLocation(project);
        File descriptorSetFile = mainDescriptorSetFile(project);
        generateFor(protoJsLocation, descriptorSetFile);
    }

    private static void generateForTest(Project project) {
        Path protoJsLocation = testProtoJsLocation(project);
        File descriptorSetFile = testDescriptorSetFile(project);
        generateFor(protoJsLocation, descriptorSetFile);
    }

    /**
     * Generates the JSON-parsing code for the proto definitions in the specified descriptor set
     * file.
     *
     * @param protoJsLocation
     *         the root of the generated JS proto definitions
     * @param descriptorSetFile
     *         the descriptor set file
     */
    private static void generateFor(Path protoJsLocation, File descriptorSetFile) {
        ProtoFromJsonWriter writer = createFor(protoJsLocation, descriptorSetFile);
        if (writer.hasFilesToProcess()) {
            writer.writeFromJsonForProtos();
        }
    }
}
