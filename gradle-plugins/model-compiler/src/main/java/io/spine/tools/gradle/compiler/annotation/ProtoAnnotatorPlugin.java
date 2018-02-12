/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.gradle.compiler.annotation;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.annotation.Beta;
import io.spine.annotation.Experimental;
import io.spine.annotation.Internal;
import io.spine.annotation.SPI;
import io.spine.tools.compiler.annotation.AnnotatorFactory;
import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.proto.FileDescriptors;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;

import java.util.Collection;

import static io.spine.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.gradle.compiler.Extension.getMainGenGrpcDir;
import static io.spine.gradle.compiler.Extension.getMainGenProtoDir;
import static io.spine.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.gradle.compiler.Extension.getTestGenGrpcDir;
import static io.spine.gradle.compiler.Extension.getTestGenProtoDir;
import static io.spine.option.OptionsProto.beta;
import static io.spine.option.OptionsProto.betaAll;
import static io.spine.option.OptionsProto.betaType;
import static io.spine.option.OptionsProto.experimental;
import static io.spine.option.OptionsProto.experimentalAll;
import static io.spine.option.OptionsProto.experimentalType;
import static io.spine.option.OptionsProto.internal;
import static io.spine.option.OptionsProto.internalAll;
import static io.spine.option.OptionsProto.internalType;
import static io.spine.option.OptionsProto.sPI;
import static io.spine.option.OptionsProto.sPIAll;
import static io.spine.option.OptionsProto.sPIService;
import static io.spine.option.OptionsProto.sPIType;
import static io.spine.tools.gradle.TaskName.ANNOTATE_PROTO;
import static io.spine.tools.gradle.TaskName.ANNOTATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.proto.FileDescriptors.isNotGoogleProto;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A plugin that annotates generated Java sources from {@code .proto} files.
 *
 * <p>Plugin annotates the Java sources depending on Protobuf option values.
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
 *
 * @author Dmytro Grankin
 */
public class ProtoAnnotatorPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        final Action<Task> task = newAction(getMainDescriptorSetPath(project), project, false);
        newTask(ANNOTATE_PROTO, task).insertBeforeTask(COMPILE_JAVA)
                                     .insertAfterTask(GENERATE_PROTO)
                                     .applyNowTo(project);
        logDependingTask(log(), ANNOTATE_PROTO, COMPILE_JAVA, GENERATE_PROTO);

        final Action<Task> testTask = newAction(getTestDescriptorSetPath(project), project, true);
        newTask(ANNOTATE_TEST_PROTO, testTask).insertBeforeTask(COMPILE_TEST_JAVA)
                                              .insertAfterTask(GENERATE_TEST_PROTO)
                                              .applyNowTo(project);
        logDependingTask(log(), ANNOTATE_TEST_PROTO, COMPILE_TEST_JAVA, GENERATE_TEST_PROTO);
    }

    private static Action<Task> newAction(final String descriptorSetPath,
                                          final Project project,
                                          final boolean isTestTask) {
        final String generatedProtoDir;
        final String generatedGrpcDir;

        if (isTestTask) {
            generatedProtoDir = getTestGenProtoDir(project);
            generatedGrpcDir = getTestGenGrpcDir(project);

        } else {
            generatedProtoDir = getMainGenProtoDir(project);
            generatedGrpcDir = getMainGenGrpcDir(project);
        }

        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                final Collection<FileDescriptorProto> descriptors =
                        FileDescriptors.parseAndFilter(descriptorSetPath, isNotGoogleProto());
                final AnnotatorFactory factory =
                        new AnnotatorFactory(descriptors, generatedProtoDir, generatedGrpcDir);

                factory.createFileAnnotator(Experimental.class, experimentalAll)
                       .annotate();
                factory.createMessageAnnotator(Experimental.class, experimentalType)
                       .annotate();
                factory.createFieldAnnotator(Experimental.class, experimental)
                       .annotate();

                factory.createFileAnnotator(Beta.class, betaAll)
                       .annotate();
                factory.createMessageAnnotator(Beta.class, betaType)
                       .annotate();
                factory.createFieldAnnotator(Beta.class, beta)
                       .annotate();

                factory.createFileAnnotator(SPI.class, sPIAll)
                       .annotate();
                factory.createMessageAnnotator(SPI.class, sPIType)
                       .annotate();
                factory.createServiceAnnotator(SPI.class, sPIService)
                       .annotate();
                factory.createFieldAnnotator(SPI.class, sPI)
                       .annotate();

                factory.createFileAnnotator(Internal.class, internalAll)
                       .annotate();
                factory.createMessageAnnotator(Internal.class, internalType)
                       .annotate();
                factory.createFieldAnnotator(Internal.class, internal)
                       .annotate();
            }
        };
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = getLogger(ProtoAnnotatorPlugin.class);
    }
}
