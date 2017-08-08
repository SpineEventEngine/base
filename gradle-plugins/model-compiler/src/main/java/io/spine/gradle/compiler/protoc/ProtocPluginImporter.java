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

package io.spine.gradle.compiler.protoc;

import com.google.common.io.Files;
import io.spine.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.collect.ImmutableMap.of;
import static io.spine.gradle.TaskName.ANALYZE_PROTO;
import static io.spine.gradle.TaskName.ANALYZE_TEST_PROTO;
import static io.spine.gradle.TaskName.GENERATE_PROTO;
import static io.spine.gradle.TaskName.GENERATE_TEST_PROTO;

/**
 * @author Dmytro Dashenkov
 */
public class ProtocPluginImporter extends SpinePlugin {

    @Override
    public void apply(Project project) {
        log().debug("Appending task {}", ANALYZE_PROTO);
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                log().debug("Appending task {} after evaluate", ANALYZE_PROTO);
                newTask(ANALYZE_PROTO, action()).insertBeforeTask(GENERATE_PROTO)
                                                .applyNowTo(project);
                newTask(ANALYZE_TEST_PROTO, action()).insertBeforeTask(GENERATE_TEST_PROTO)
                                                     .applyNowTo(project);
            }
        });

    }

    private static Action<Task> action() {
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                final Project project = task.getProject();
                addExtension(project, "protoPluginPath",
                             "/Users/ddashenkov/Documents/dev/java/examples/grpc-java/compiler/build/exe/java_plugin/protoc-gen-grpc-java");
                final File tempFolder = Files.createTempDir();
                final File configFile = new File(tempFolder, "protoc_config.gradle");
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("protoc_config.gradle");
                     FileOutputStream out = new FileOutputStream(configFile)) {
                    int aByte = in.read();
                    while (aByte >= 0) {
                        out.write(aByte);
                        aByte = in.read();
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }

                log().debug("Applying protoc_config.gradle ({})", configFile.getAbsolutePath());
                project.apply(of("from", configFile.getAbsolutePath()));
                log().debug("Applied protoc_config.gradle");
            }
        };
    }

    private static void addExtension(Project project, String key, Object value) {
        final ExtensionContainer ext = project.getExtensions();
        if (ext.findByName(key) == null) {
            project.getExtensions().add(key, value);
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ProtocPluginImporter.class);
    }
}
