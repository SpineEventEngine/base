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

package io.spine.tools.mc.java.gradle;

import io.spine.tools.debug.DebugBox;
import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.mc.java.annotation.Annotator;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.gradle.JavaTaskName.compileTestJava;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.annotateProto;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.annotateTestProto;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.mergeDescriptorSet;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.mergeTestDescriptorSet;

/**
 * A plugin that annotates Java sources generated from {@code .proto} files.
 *
 * <p>Plugin annotates the Java sources depending on Protobuf option values such
 * as {@code (internal)} set for a field, or {@code (experimental_type)} on a {@code Message}.
 * For the full list of options, please see {@code spine/options.proto}.
 *
 * <p>To enable the Java sources annotation, apply the plugin to a Gradle project,
 * and annotation will be built into Gradle build lifecycle,
 * between Protobuf generation and Java compilation.
 *
 * <p>Examples:
 *
 * <p>For {@code FileOptions}:
 *
 * <pre>{@code
 * import "spine/options.proto";
 *
 * option (experimental_all) = true;
 *
 * message Message {
 * }
 *
 * service Service {
 * }
 * }</pre>
 *
 * <p>Will annotate regular generated file like:
 *
 * <pre>{@code
 * OuterClassName {
 *
 *      // Annotation goes here.
 *      public static final class Message ...
 *
 *      // Annotation goes here.
 *      public interface MessageOrBuilder ...
 *
 *      // And so on for every message and enum from a Protobuf file.
 * }}</pre>
 *
 * <p>And generated gPRC service like:
 *
 * <pre>{@code
 * // Annotation goes here.
 * public class ServiceGrpc {
 *      // ...
 * }}</pre>
 *
 * <p>For {@code MessageOptions}:
 *
 * <pre>{@code
 * import "spine/options.proto";
 *
 * message Message {
 *      option (experimental_type) = true;
 * }}</pre>
 *
 * <p>Will annotate generated file like:
 *
 * <pre>{@code
 * OuterClassName {
 *
 *      // Annotation goes here.
 *      public static final class Message ...
 *
 *      // Annotation goes here.
 *      public interface MessageOrBuilder ...
 * }}</pre>
 *
 * <p>For {@code ServiceOptions}:
 *
 * <pre>{@code
 * import "spine/options.proto";
 *
 * service Service {
 *      option (SPI_service) = true;
 * }}</pre>
 *
 * <p>Will annotate generated gRPC service like:
 *
 * <pre>{@code
 * // Annotation goes here.
 * ServiceGrpc {
 *      // ...
 * }}</pre>
 *
 * <p>For {@code FieldOptions}:
 *
 * <pre>{@code
 * import "spine/options.proto";
 *
 * message Message {
 *      string value = 1 [(experimental) = true] ;
 * }}</pre>
 *
 * <p>Will annotate generated file like:
 *
 * <pre>{@code
 * OuterClassName {
 *
 *      public static final class Message ... {
 *
 *              // Annotation goes here.
 *              public java.lang.String getEntityId() {
 *                  // ...
 *                  }
 *
 *              // And so on for every getter for the field.
 *
 *              public static final class Builder ... {
 *
 *                  // Annotation goes here.
 *                  public java.lang.String getEntityId() {
 *                      // ...
 *                  }
 *
 *                  // Annotation goes here.
 *                  public java.lang.String setEntityId() {
 *                      // ...
 *                  }
 *
 *                  // And so on for every getter/setter for the field.
 *              }
 *      }
 * }}</pre>
 *
 * <p>If {@code java_multiple_files = true} result of annotation will be similar.
 */
public class AnnotatorPlugin extends SpinePlugin {

    public AnnotatorPlugin() {
        DebugBox.show("Annotation Plugin", "Created!");
    }
    
    @Override
    public void apply(Project project) {
        DebugBox.show("Annotation Plugin", "Creating task.");
        createMainTask(project);
        createTestTask(project);
    }

    private void createMainTask(Project project) {
        Action<Task> task = new AnnotationAction(this, true);
        newTask(annotateProto, task)
                .insertAfterTask(mergeDescriptorSet)
                .insertBeforeTask(compileJava)
                .applyNowTo(project);
    }

    private void createTestTask(Project project) {
        Action<Task> testTask = new AnnotationAction(this, false);
        newTask(annotateTestProto, testTask)
                .insertAfterTask(mergeTestDescriptorSet)
                .insertBeforeTask(compileTestJava)
                .applyNowTo(project);
    }
}

