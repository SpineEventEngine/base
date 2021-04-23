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

package io.spine.tools.mc.java.code.annotation;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.tools.java.fs.SourceFile;
import io.spine.code.java.ClassName;
import io.spine.code.java.SimpleClassName;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import java.nio.file.Path;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Annotates generated top-level messages, if a specified {@linkplain ApiOption message option}
 * value is {@code true} in a {@code .proto} file declaring these types.
 */
final class MessageAnnotator extends OptionAnnotator<Descriptor> {

    MessageAnnotator(ClassName annotation,
                     ApiOption option,
                     ImmutableList<FileDescriptor> files,
                     Path genProtoDir) {
        super(annotation, option, files, genProtoDir);
    }

    @Override
    public final void annotate() {
        for (FileDescriptor file : descriptors()) {
            annotate(file);
        }
    }

    @Override
    protected final void annotateOneFile(FileDescriptor file) {
        SourceFile outerClass = SourceFile.forOuterClassOf(file.toProto());
        rewriteSource(outerClass, new AnnotateNestedType(file));
    }

    @Override
    protected final void annotateMultipleFiles(FileDescriptor file) {
        for (Descriptor definitionDescriptor : getDefinitions(file)) {
            if (shouldAnnotate(definitionDescriptor)) {
                annotateMessageTypes(definitionDescriptor, file);
            }
        }
    }

    private static List<Descriptor> getDefinitions(FileDescriptor file) {
        return file.getMessageTypes();
    }

    @Override
    protected boolean shouldAnnotate(Descriptor descriptor) {
        return option().isPresentAt(descriptor);
    }

    static <T extends JavaSource<T>>
    JavaSource findNestedType(AbstractJavaSource<T> enclosingClass, String typeName) {
        for (JavaSource nestedType : enclosingClass.getNestedTypes()) {
            if (nestedType.getName()
                          .equals(typeName)) {
                return nestedType;
            }
        }

        String errMsg = format("Nested type `%s` is not defined in `%s`.",
                               typeName, enclosingClass.getName());
        throw new IllegalStateException(errMsg);
    }

    /**
     * Optionally annotates nested types in a file.
     */
    private class AnnotateNestedType implements SourceVisitor<JavaClassSource> {

        private final FileDescriptor file;

        private AnnotateNestedType(FileDescriptor file) {
            this.file = file;
        }

        @Override
        public void accept(@Nullable AbstractJavaSource<JavaClassSource> input) {
            checkNotNull(input);
            for (Descriptor definition : getDefinitions(file)) {
                if (shouldAnnotate(definition)) {
                    String messageName = definition.getName();
                    JavaSource message = findNestedType(input, messageName);
                    addAnnotation(message);

                    String javaType = SimpleClassName.messageOrBuilder(messageName)
                                                     .value();
                    JavaSource messageOrBuilder = findNestedType(input, javaType);
                    addAnnotation(messageOrBuilder);
                }
            }
        }
    }
}
