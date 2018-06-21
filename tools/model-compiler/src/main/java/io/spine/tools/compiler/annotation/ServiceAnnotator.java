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

package io.spine.tools.compiler.annotation;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.code.java.SourceFile;
import io.spine.option.Options;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

/**
 * An annotator for {@code gRPC} services.
 *
 * <p>Annotates a service in a generated Java source
 * if a specified {@linkplain com.google.protobuf.DescriptorProtos.ServiceOptions service option}
 * value is {@code true} for a service definition.
 *
 * @author Dmytro Grankin
 */
class ServiceAnnotator extends Annotator<ServiceOptions, ServiceDescriptorProto> {

    ServiceAnnotator(Class<? extends Annotation> annotation,
                     GeneratedExtension<ServiceOptions, Boolean> option,
                     Collection<FileDescriptorProto> fileDescriptors,
                     String genProtoDir) {
        super(annotation, option, fileDescriptors, genProtoDir);
    }

    @Override
    public void annotate() {
        for (FileDescriptorProto fileDescriptor : fileDescriptors()) {
            annotate(fileDescriptor);
        }
    }

    @Override
    protected void annotateOneFile(FileDescriptorProto fileDescriptor) {
        annotateServices(fileDescriptor);
    }

    @Override
    protected void annotateMultipleFiles(FileDescriptorProto fileDescriptor) {
        annotateServices(fileDescriptor);
    }

    private void annotateServices(FileDescriptorProto file) {
        for (ServiceDescriptorProto serviceDescriptor : file.getServiceList()) {
            if (shouldAnnotate(serviceDescriptor)) {
                final SourceFile serviceClass = SourceFile.forService(serviceDescriptor, file);
                rewriteSource(serviceClass, new TypeDeclarationAnnotation());
            }
        }
    }

    @Override
    protected Optional<Boolean> getOptionValue(ServiceDescriptorProto descriptor) {
        return Options.option(descriptor, getOption());
    }
}
