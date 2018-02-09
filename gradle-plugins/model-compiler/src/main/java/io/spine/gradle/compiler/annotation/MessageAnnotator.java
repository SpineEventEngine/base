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

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.tools.java.JavaSources;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static io.spine.option.UnknownOptions.getUnknownOptionValue;

/**
 * A message annotator.
 *
 * <p>Annotates generated top-level messages from a {@code .proto} file,
 * if a specified {@linkplain com.google.protobuf.DescriptorProtos.MessageOptions message option}
 * value is {@code true}.
 *
 * @author Dmytro Grankin
 */
class MessageAnnotator extends TypeDefinitionAnnotator<MessageOptions, DescriptorProto> {

    MessageAnnotator(Class<? extends Annotation> annotation,
                     GeneratedExtension<MessageOptions, Boolean> option,
                     Collection<FileDescriptorProto> fileDescriptors,
                     String genProtoDir) {
        super(annotation, option, fileDescriptors, genProtoDir);
    }

    @Override
    protected List<DescriptorProto> getDefinitions(FileDescriptorProto fileDescriptor) {
        return fileDescriptor.getMessageTypeList();
    }

    @Override
    protected String getDefinitionName(DescriptorProto definitionDescriptor) {
        return definitionDescriptor.getName();
    }

    @Override
    protected void annotateDefinition(DescriptorProto definitionDescriptor,
                                      FileDescriptorProto fileDescriptor) {
        final Path messageFilePath = JavaSources.getFile(definitionDescriptor, false,
                                                         fileDescriptor);
        rewriteSource(messageFilePath, new TypeDeclarationAnnotation());

        final Path messageOrBuilderPath = JavaSources.getFile(definitionDescriptor, true,
                                                              fileDescriptor);
        rewriteSource(messageOrBuilderPath, new TypeDeclarationAnnotation());
    }

    @Override
    protected String getRawOptionValue(DescriptorProto descriptor) {
        return getUnknownOptionValue(descriptor, getOptionNumber());
    }
}
