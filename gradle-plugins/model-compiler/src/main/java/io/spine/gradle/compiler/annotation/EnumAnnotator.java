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

package io.spine.gradle.compiler.annotation;

import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static io.spine.gradle.compiler.util.JavaSources.getFilePath;
import static io.spine.protobuf.UnknownOptions.getUnknownOptionValue;

/**
 * An enum annotator.
 *
 * <p>Annotates generated top-level enums from a {@code .proto} file,
 * if a specified {@linkplain com.google.protobuf.DescriptorProtos.EnumOptions enum option}
 * value is {@code true}.
 *
 * @author Dmytro Grankin
 */
class EnumAnnotator extends TypeDefinitionAnnotator<EnumOptions, EnumDescriptorProto> {

    EnumAnnotator(Class<? extends Annotation> annotation,
                  GeneratedExtension<EnumOptions, Boolean> option,
                  Collection<FileDescriptorProto> fileDescriptors,
                  String genProtoDir) {
        super(annotation, option, fileDescriptors, genProtoDir);
    }

    @Override
    protected List<EnumDescriptorProto> getDefinitions(FileDescriptorProto fileDescriptor) {
        return fileDescriptor.getEnumTypeList();
    }

    @Override
    protected String getDefinitionName(EnumDescriptorProto definitionDescriptor) {
        return definitionDescriptor.getName();
    }

    @Override
    protected String getRawOptionValue(EnumDescriptorProto descriptor) {
        return getUnknownOptionValue(descriptor, getOptionNumber());
    }

    @Override
    protected void annotateDefinition(EnumDescriptorProto definitionDescriptor,
                                      FileDescriptorProto fileDescriptor) {
        final Path filePath = getFilePath(definitionDescriptor, fileDescriptor);
        rewriteSource(filePath, new TypeDeclarationAnnotation());
    }
}
