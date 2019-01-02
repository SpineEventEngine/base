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

import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.option.Options;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A message annotator.
 *
 * <p>Annotates generated top-level messages from a {@code .proto} file,
 * if a specified {@linkplain MessageOptions message option} value is {@code true}.
 */
class MessageAnnotator extends TypeDefinitionAnnotator<MessageOptions, Descriptor> {

    MessageAnnotator(Class<? extends Annotation> annotation,
                     GeneratedExtension<MessageOptions, Boolean> option,
                     Collection<FileDescriptor> files,
                     String genProtoDir) {
        super(annotation, option, files, genProtoDir);
    }

    @Override
    protected List<Descriptor> getDefinitions(FileDescriptor file) {
        return file.getMessageTypes();
    }

    @Override
    protected String getDefinitionName(Descriptor definition) {
        return definition.getName();
    }

    @Override
    protected void annotateDefinition(Descriptor definition, FileDescriptor file) {
        annotateMessageTypes(definition, file);
    }

    @Override
    protected Optional<Boolean> getOptionValue(Descriptor definition) {
        return Options.option(definition, getOption());
    }
}
