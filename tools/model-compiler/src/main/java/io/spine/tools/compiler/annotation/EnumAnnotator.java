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

import com.google.protobuf.DescriptorProtos.EnumOptions;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.code.java.SourceFile;
import io.spine.option.Options;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * An enum annotator.
 *
 * <p>Annotates generated top-level enums from a {@code .proto} file,
 * if a specified {@linkplain com.google.protobuf.DescriptorProtos.EnumOptions enum option}
 * value is {@code true}.
 */
class EnumAnnotator extends TypeDefinitionAnnotator<EnumOptions, EnumDescriptor> {

    EnumAnnotator(Class<? extends Annotation> annotation,
                  GeneratedExtension<EnumOptions, Boolean> option,
                  Collection<FileDescriptor> files,
                  String genProtoDir) {
        super(annotation, option, files, genProtoDir);
    }

    @Override
    protected List<EnumDescriptor> getDefinitions(FileDescriptor file) {
        return file.getEnumTypes();
    }

    @Override
    protected String getDefinitionName(EnumDescriptor enumType) {
        return enumType.getName();
    }

    @Override
    protected Optional<Boolean> getOptionValue(EnumDescriptor descriptor) {
        return Options.option(descriptor, getOption());
    }

    @Override
    protected void annotateDefinition(EnumDescriptor enumType, FileDescriptor file) {
        SourceFile enumFile = SourceFile.forEnum(enumType.toProto(), file.toProto());
        annotate(enumFile);
    }
}
