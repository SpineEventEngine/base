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
package io.spine.gradle.compiler.lookup.proto;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.gradle.SpinePlugin;
import io.spine.gradle.compiler.util.PropertiesWriter;
import io.spine.tools.proto.FileDescriptors;
import io.spine.type.KnownTypes;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static io.spine.gradle.TaskName.GENERATE_PROTO;
import static io.spine.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.gradle.TaskName.MAP_PROTO_TO_JAVA;
import static io.spine.gradle.TaskName.MAP_TEST_PROTO_TO_JAVA;
import static io.spine.gradle.TaskName.PROCESS_RESOURCES;
import static io.spine.gradle.TaskName.PROCESS_TEST_RESOURCES;
import static io.spine.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.gradle.compiler.Extension.getMainTargetGenResourcesDir;
import static io.spine.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.gradle.compiler.Extension.getTestTargetGenResourcesDir;
import static io.spine.tools.proto.FileDescriptors.isNotGoogleProto;

/**
 * Plugin which maps all Protobuf types to the corresponding Java classes.
 *
 * <p>Generates a {@code .properties} file, which contains entries like:
 *
 * <p>{@code PROTO_TYPE_URL=JAVA_FULL_CLASS_NAME}
 *
 * @author Mikhail Mikhaylov
 * @author Alexander Yevsyukov
 * @author Alexander Litus
 * @author Alex Tymchenko
 */
public class ProtoToJavaMapperPlugin extends SpinePlugin {

    /**
     * The name of the file to populate. NOTE: also change its name used
     * in the `core-java` project on changing.
     */
    private static final String PROPERTIES_FILE_NAME = KnownTypes.PROPS_FILE_PATH;

    /**
     * Adds tasks to map Protobuf types to Java classes in the project.
     */
    @Override
    public void apply(final Project project) {
        final Action<Task> mainScopeAction = mainScopeActionFor(project);
        logDependingTask(log(), MAP_PROTO_TO_JAVA, PROCESS_RESOURCES, GENERATE_PROTO);
        final GradleTask mainScopeTask =
                newTask(MAP_PROTO_TO_JAVA, mainScopeAction).insertAfterTask(GENERATE_PROTO)
                                                           .insertBeforeTask(PROCESS_RESOURCES)
                                                           .applyNowTo(project);

        final Action<Task> testScopeAction = testScopeActionFor(project);
        logDependingTask(log(), MAP_TEST_PROTO_TO_JAVA, PROCESS_TEST_RESOURCES,
                         GENERATE_TEST_PROTO);
        final GradleTask testScopeTask =
                newTask(MAP_TEST_PROTO_TO_JAVA,
                        testScopeAction).insertAfterTask(GENERATE_TEST_PROTO)
                                        .insertBeforeTask(PROCESS_TEST_RESOURCES)
                                        .applyNowTo(project);

        log().debug("Proto-to-Java mapping phase initialized with tasks: {}, {}",
                    mainScopeTask, testScopeTask);
    }

    private static Action<Task> testScopeActionFor(final Project project) {
        log().debug("Initializing the proto to java mapping for the \"test\" source code");
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                mapProtoToJavaAndWriteProps(getTestTargetGenResourcesDir(project),
                                            getTestDescriptorSetPath(project));
            }
        };
    }

    private static Action<Task> mainScopeActionFor(final Project project) {
        log().debug("Initializing the proto to java mapping for the \"main\" source code.");
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                mapProtoToJavaAndWriteProps(getMainTargetGenResourcesDir(project),
                                            getMainDescriptorSetPath(project));
            }
        };
    }

    private static void mapProtoToJavaAndWriteProps(String targetGeneratedResourcesDir,
                                                    String descriptorSetPath) {
        final Map<String, String> propsMap = newHashMap();
        final Collection<FileDescriptorProto> files =
                FileDescriptors.parseAndFilter(descriptorSetPath, isNotGoogleProto());
        log().trace("Starting mapping files under: {}", files);
        for (FileDescriptorProto file : files) {
            log().debug("Looking up file {}", file.getName());
            final Map<String, String> types = new ProtoToJavaTypeMapper(file).mapTypes();
            propsMap.putAll(types);
        }
        if (propsMap.isEmpty()) {
            log().debug("No proto types found. Searched under: {}", files);
            return;
        }

        log().debug("{} types found", files.size());
        log().trace("Saving proto-to-java mapping: {}", files);

        final PropertiesWriter writer = new PropertiesWriter(targetGeneratedResourcesDir,
                                                             PROPERTIES_FILE_NAME);
        writer.write(propsMap);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ProtoToJavaMapperPlugin.class);
    }
}
