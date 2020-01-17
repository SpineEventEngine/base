/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.compiler.annotation;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.spine.code.fs.java.SourceFile;
import io.spine.code.java.ClassName;

import java.nio.file.Path;

import static io.spine.code.fs.java.SourceFile.forService;

/**
 * An annotator for {@code gRPC} services.
 *
 * <p>Annotates a service in a generated Java source
 * if a specified {@linkplain com.google.protobuf.DescriptorProtos.ServiceOptions service option}
 * value is {@code true} for a service definition.
 */
final class ServiceAnnotator extends OptionAnnotator<ServiceDescriptor> {

    ServiceAnnotator(ClassName annotation,
                     ApiOption option,
                     ImmutableList<FileDescriptor> fileDescriptors,
                     Path genProtoDir) {
        super(annotation, option, fileDescriptors, genProtoDir);
    }

    @Override
    public void annotate() {
        for (FileDescriptor fileDescriptor : descriptors()) {
            annotate(fileDescriptor);
        }
    }

    @Override
    protected void annotateOneFile(FileDescriptor fileDescriptor) {
        annotateServices(fileDescriptor);
    }

    @Override
    protected void annotateMultipleFiles(FileDescriptor fileDescriptor) {
        annotateServices(fileDescriptor);
    }

    private void annotateServices(FileDescriptor file) {
        for (ServiceDescriptor serviceDescriptor : file.getServices()) {
            if (shouldAnnotate(serviceDescriptor)) {
                SourceFile serviceClass = forService(serviceDescriptor.toProto(), file.toProto());
                annotate(serviceClass);
            }
        }
    }

    @Override
    protected boolean shouldAnnotate(ServiceDescriptor descriptor) {
        return option().isPresentAt(descriptor);
    }
}
