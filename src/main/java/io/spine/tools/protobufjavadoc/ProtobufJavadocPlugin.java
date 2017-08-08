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
package io.spine.tools.protobufjavadoc;

import io.spine.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.gradle.TaskName.FORMAT_PROTO_DOC;
import static io.spine.gradle.TaskName.FORMAT_TEST_PROTO_DOC;
import static io.spine.gradle.TaskName.GENERATE_PROTO;
import static io.spine.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.protobufjavadoc.Extension.getMainGenProtoDir;
import static io.spine.tools.protobufjavadoc.Extension.getTestGenProtoDir;

/**
 * The plugin, that formats Javadocs in sources generated from {@code .proto} files.
 *
 * <p>Does the following formatting:
 * <ul>
 *     <li>removes all {@code <pre>} tags;</li>
 *     <li>replaces a text in back ticks by the text in {@code code} tag.</li>
 * </ul>
 *
 *  @author Alexander Aleksandrov
 *  @author Dmytro Grankin
 */
public class ProtobufJavadocPlugin extends SpinePlugin {

    static final String PROTO_JAVADOC_EXTENSION_NAME = "protoJavadoc";

    @Override
    public void apply(final Project project) {
        log().debug("Adding the ProtoJavadocPlugin extension to the project.");
        project.getExtensions()
               .create(PROTO_JAVADOC_EXTENSION_NAME, Extension.class);

        final String mainGenProtoDir = getMainGenProtoDir(project);
        final Action<Task> mainAction = createAction(mainGenProtoDir);
        newTask(FORMAT_PROTO_DOC, mainAction).insertBeforeTask(COMPILE_JAVA)
                                             .insertAfterTask(GENERATE_PROTO)
                                             .applyNowTo(project);
        logDependingTask(log(), FORMAT_PROTO_DOC, COMPILE_JAVA, GENERATE_PROTO);

        final String testGenProtoDir = getTestGenProtoDir(project);
        final Action<Task> testAction = createAction(testGenProtoDir);
        newTask(FORMAT_TEST_PROTO_DOC, testAction).insertBeforeTask(COMPILE_TEST_JAVA)
                                                  .insertAfterTask(GENERATE_TEST_PROTO)
                                                  .applyNowTo(project);
        logDependingTask(log(), FORMAT_TEST_PROTO_DOC, COMPILE_TEST_JAVA, GENERATE_TEST_PROTO);
    }

    private static Action<Task> createAction(final String javaSourcesPath) {
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                formatJavadocs(javaSourcesPath);
            }
        };
    }

    private static void formatJavadocs(String javaSourcesDir) {
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ProtobufJavadocPlugin.class);
    }
}
