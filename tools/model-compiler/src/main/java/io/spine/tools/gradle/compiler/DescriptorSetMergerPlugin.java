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

package io.spine.tools.gradle.compiler;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileSet;
import io.spine.tools.gradle.ProtoPlugin;
import io.spine.tools.type.MergedDescriptorSet;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_VALIDATING_BUILDERS;
import static io.spine.tools.gradle.TaskName.MERGE_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.MERGE_TEST_DESCRIPTOR_SET;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSetPath;
import static java.util.stream.Collectors.toList;

/**
 * A Gradle plugin which merges the descriptor file with all the descriptor files from
 * the project runtime classpath.
 *
 * <p>The merge result is used to {@linkplain
 * io.spine.tools.type.MoreKnownTypes#extendWith(java.io.File) extend the known type registry}.
 */
public class DescriptorSetMergerPlugin extends ProtoPlugin {

    @Override
    public void apply(Project project) {
        createMainTask(project);
        createTestTask(project);
    }

    private void createMainTask(Project project) {
        String descriptorSetPath = getMainDescriptorSetPath(project);
        FileSet fileSet = mainProtoFiles(project);
        newTask(MERGE_DESCRIPTOR_SET,
                createMergingAction(descriptorSetPath, fileSet))
                .insertAfterTask(GENERATE_PROTO)
                .applyNowTo(project);
    }

    private void createTestTask(Project project) {
        String descriptorSetPath = getTestDescriptorSetPath(project);
        FileSet fileSet = testProtoFiles(project);
        newTask(MERGE_TEST_DESCRIPTOR_SET,
                createMergingAction(descriptorSetPath, fileSet))
                .insertAfterTask(GENERATE_TEST_PROTO)
                .insertBeforeTask(GENERATE_TEST_VALIDATING_BUILDERS)
                .applyNowTo(project);
    }

    @Override
    protected Supplier<String> mainDescriptorSetPath(Project project) {
        return () -> getMainDescriptorSetPath(project);
    }

    @Override
    protected Supplier<String> testDescriptorSetPath(Project project) {
        return () -> getTestDescriptorSetPath(project);
    }

    private static Action<Task> createMergingAction(String descriptorSetPath, FileSet fileSet) {
        return task -> {
            List<FileDescriptorProto> files = fileSet
                    .files()
                    .stream()
                    .map(FileDescriptor::toProto)
                    .collect(toList());
            FileDescriptorSet descriptorSet = FileDescriptorSet
                    .newBuilder()
                    .addAllFile(files)
                    .build();
            File descriptorSetFile = new File(descriptorSetPath);
            new MergedDescriptorSet(descriptorSet).writeTo(descriptorSetFile);
        };
    }
}
