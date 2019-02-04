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
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.io.Files.createParentDirs;
import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_VALIDATING_BUILDERS;
import static io.spine.tools.gradle.TaskName.MERGE_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.MERGE_TEST_DESCRIPTOR_SET;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSet;
import static io.spine.util.Exceptions.illegalArgumentWithCauseOf;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
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
        newTask(MERGE_DESCRIPTOR_SET,
                createMergingAction(mainDescriptorFile(project), mainProtoFiles(project)))
                .insertAfterTask(GENERATE_PROTO)
                .applyNowTo(project);
    }

    private void createTestTask(Project project) {
        newTask(MERGE_TEST_DESCRIPTOR_SET,
                createMergingAction(testDescriptorFile(project), testProtoFiles(project)))
                .insertAfterTask(GENERATE_TEST_PROTO)
                .insertBeforeTask(GENERATE_TEST_VALIDATING_BUILDERS)
                .applyNowTo(project);
    }

    @Override
    protected Supplier<File> mainDescriptorFile(Project project) {
        return () -> getMainDescriptorSet(project);
    }

    @Override
    protected Supplier<File> testDescriptorFile(Project project) {
        return () -> getTestDescriptorSet(project);
    }

    private static Action<Task> createMergingAction(Supplier<File> descriptorSetFile,
                                                    Supplier<FileSet> fileSet) {
        return task -> {
            List<FileDescriptorProto> files = fileSet
                    .get()
                    .files()
                    .stream()
                    .map(FileDescriptor::toProto)
                    .collect(toList());
            FileDescriptorSet descriptorSet = FileDescriptorSet
                    .newBuilder()
                    .addAllFile(files)
                    .build();
            write(descriptorSet, descriptorSetFile.get());
        };
    }

    /**
     * Writes this descriptor set into the given file.
     *
     * <p>If the file exists, it will be overridden. Otherwise, the file (and all its parent
     * directories if necessary) will be created.
     *
     * @param destination
     *         the file to write this descriptor set into
     */
    private static void write(FileDescriptorSet descriptorSet, File destination) {
        prepareFile(destination);
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(destination))) {
            descriptorSet.writeTo(out);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static void prepareFile(File destination) {
        try {
            destination.delete();
            createParentDirs(destination);
            destination.createNewFile();
        } catch (IOException e) {
            throw illegalArgumentWithCauseOf(e);
        }
    }
}
