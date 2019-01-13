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

package io.spine.js.gradle;

import com.google.common.annotations.VisibleForTesting;
import io.spine.code.js.DefaultJsProject;
import io.spine.code.js.Directory;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * An extension for the {@link ProtoJsPlugin} which allows to obtain the {@code generateJsonParsers}
 * task to configure when it will be executed during the build lifecycle.
 */
@SuppressWarnings({"PublicField", "WeakerAccess"} /* Expose fields as a Gradle extension */)
public class Extension {

    private Task generateParsersTask;

    /**
     * The absolute path to the main Protobuf descriptor set file.
     */
    public String mainDescriptorSetPath;
    /**
     * The absolute path to the test Protobuf descriptor set file.
     */
    public String testDescriptorSetPath;
    /**
     * The absolute path to the main Protobufs compiled to Javascript.
     */
    public String mainGenProtoDir;
    /**
     * The absolute path to the test Protobufs compiled to Javascript.
     */
    public String testGenProtoDir;

    public static Directory getMainGenProto(Project project) {
        Extension extension = extension(project);
        String specifiedValue = extension.mainGenProtoDir;
        Path path = pathOrDefault(specifiedValue,
                                  def(project).proto()
                                              .mainJs());
        return Directory.at(path);
    }

    public static Directory getTestGenProtoDir(Project project) {
        Extension extension = extension(project);
        String specifiedValue = extension.testGenProtoDir;
        Path path = pathOrDefault(specifiedValue,
                                  def(project).proto()
                                              .testJs());
        return Directory.at(path);
    }

    public static File getMainDescriptorSet(Project project) {
        Path path = pathOrDefault(extension(project).mainDescriptorSetPath,
                                  def(project).mainDescriptors());
        return path.toFile();
    }

    public static File getTestDescriptorSet(Project project) {
        Path path = pathOrDefault(extension(project).testDescriptorSetPath,
                                  def(project).testDescriptors());
        return path.toFile();
    }

    /**
     * Returns the {@code generateJsonParsers} task configured by the {@link ProtoJsPlugin}.
     */
    @SuppressWarnings("unused") // Used in project applying the plugin.
    public Task generateParsersTask() {
        checkState(generateParsersTask != null,
                   "The 'generateJsonParsers' task was not configured by the ProtoJS plugin");
        return generateParsersTask;
    }

    /**
     * Makes the extension read-only for all plugin users.
     */
    void setGenerateParsersTask(Task generateParsersTask) {
        this.generateParsersTask = generateParsersTask;
    }

    @VisibleForTesting
    static Extension extension(Project project) {
        return (Extension)
                project.getExtensions()
                       .getByName(ProtoJsPlugin.extensionName());
    }

    private static Path pathOrDefault(String path, Object defaultValue) {
        String pathValue = isNullOrEmpty(path)
                           ? defaultValue.toString()
                           : path;
        return Paths.get(pathValue);
    }

    private static DefaultJsProject def(Project project) {
        return DefaultJsProject.at(project.getProjectDir());
    }
}
