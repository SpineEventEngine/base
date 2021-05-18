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

package io.spine.tools.mc.java.annotation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.tools.java.fs.SourceFile;
import io.spine.code.java.ClassName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.mc.java.field.FieldType;
import io.spine.tools.mc.java.field.GeneratedAccessors;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.tools.mc.java.annotation.MessageAnnotator.findNestedType;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Annotates field accessor in a generated Java code.
 *
 * <p>Annotates {@code public} accessors for a field in a generated Java source
 * if a specified {@linkplain FieldOptions field option} value is {@code true}
 * for the field definition.
 */
final class FieldAnnotator extends OptionAnnotator<FieldDescriptor> {

    FieldAnnotator(ClassName annotation,
                   ApiOption option,
                   ImmutableList<FileDescriptor> fileDescriptors,
                   Path genProtoDir) {
        super(annotation, option, fileDescriptors, genProtoDir);
    }

    @Override
    public void annotate() {
        for (FileDescriptor file : descriptors()) {
            annotate(file);
        }
    }

    @Override
    protected void annotateOneFile(FileDescriptor file) {
        if (shouldAnnotate(file)) {
            SourceFile outerClass = SourceFile.forOuterClassOf(file.toProto());
            rewriteSource(outerClass, new FileFieldAnnotation(file));
        }
    }

    @Override
    protected void annotateMultipleFiles(FileDescriptor file) {
        for (Descriptor messageType : file.getMessageTypes()) {
            if (shouldAnnotate(messageType)) {
                SourceVisitor<JavaClassSource> annotation = new MessageFieldAnnotation(messageType);
                SourceFile filePath = SourceFile.forMessage(messageType.toProto(), file.toProto());
                rewriteSource(filePath, annotation);
            }
        }
    }

    @Override
    protected boolean shouldAnnotate(FieldDescriptor descriptor) {
        return option().isPresentAt(descriptor);
    }

    /**
     * Casts a {@link JavaType} to a {@link JavaClassSource}.
     *
     * @param javaType the type to cast
     * @return a casted instance
     * @throws IllegalStateException if the specified source is not a class
     */
    private static JavaClassSource castToClass(JavaType<?> javaType) {
        checkState(javaType.isClass(), "`%s expected to be a class.", javaType.getQualifiedName());
        return (JavaClassSource) javaType;
    }

    /**
     * An annotation function for the {@link #fileDescriptor}.
     */
    private class FileFieldAnnotation implements SourceVisitor<JavaClassSource> {

        /**
         * A file descriptor, that has {@code false} value for a {@code java_multiple_files} option.
         */
        private final FileDescriptor fileDescriptor;

        private FileFieldAnnotation(FileDescriptor file) {
            checkMultipleFilesOption(file, false);
            this.fileDescriptor = file;
        }

        /**
         * Annotates the accessors, which should be annotated, within the specified input.
         *
         * @param input the {@link AbstractJavaSource} for the {@link #fileDescriptor}
         */
        @Override
        public void accept(AbstractJavaSource<JavaClassSource> input) {
            checkNotNull(input);
            for (Descriptor messageType : fileDescriptor.getMessageTypes()) {
                processMessageDescriptor(input, messageType);
            }
        }

        private void processMessageDescriptor(AbstractJavaSource<JavaClassSource> input,
                                              Descriptor messageType) {
            for (FieldDescriptor field : messageType.getFields()) {
                if (shouldAnnotate(field)) {
                    JavaSource<?> message = findNestedType(input, messageType.getName());
                    annotateMessageField(castToClass(message), new FieldDeclaration(field));
                }
            }
        }
    }

    /**
     * An annotation function for a {@link #message}.
     */
    class MessageFieldAnnotation implements SourceVisitor<JavaClassSource> {

        /**
         * A message descriptor for a file descriptor,
         * that has {@code true} value for a {@code java_multiple_files} option.
         */
        private final Descriptor message;

        private MessageFieldAnnotation(Descriptor message) {
            checkMultipleFilesOption(message.getFile(), true);

            this.message = message;
        }

        /**
         * Annotates the accessors, which should be annotated, within the specified input.
         *
         * @param input the {@link AbstractJavaSource} for the {@link #message}
         */
        @Override
        public void accept(AbstractJavaSource<JavaClassSource> input) {
            checkNotNull(input);
            for (FieldDescriptor field : message.getFields()) {
                if (shouldAnnotate(field)) {
                    annotateMessageField(castToClass(input), new FieldDeclaration(field));
                }
            }
        }
    }

    /**
     * Annotates the accessors for the specified field.
     *
     * @param message
     *        the message, that contains field for annotation
     * @param field
     *        the field descriptor to get field name
     */
    private void annotateMessageField(JavaClassSource message,
                                      FieldDeclaration field) {
        JavaClassSource messageBuilder = builderOf(message);
        annotateAccessors(message, field);
        annotateAccessors(messageBuilder, field);
    }

    private static JavaClassSource builderOf(JavaClassSource messageSource) {
        JavaSource<?> builderSource = messageSource.getNestedType(SimpleClassName.ofBuilder()
                                                                                 .value());
        return castToClass(builderSource);
    }

    /**
     * Annotates {@code public} accessors for the specified field.
     *
     * @param javaSource
     *        class source to modify
     * @param field
     *        the declaration of the field to be annotated
     */
    private void annotateAccessors(JavaClassSource javaSource,
                                   FieldDeclaration field) {
        ImmutableSet<String> names = GeneratedAccessors.forField(field.name(), FieldType.of(field))
                                                       .names();
        javaSource.getMethods()
                  .stream()
                  .filter(MethodSource::isPublic)
                  .filter(method -> names.contains(method.getName()))
                  .forEach(this::addAnnotation);
    }

    /**
     * Tells whether the specified file descriptor contains at least
     * a message descriptor with at least a field, that should be annotated.
     *
     * @param file the file descriptor to scan
     * @return {@code true} if the file descriptor contains fields for annotation
     */
    private boolean shouldAnnotate(FileDescriptor file) {
        return file.getMessageTypes()
                   .stream()
                   .anyMatch(this::shouldAnnotate);
    }

    /**
     * Tells whether the specified message descriptor contains at least a field,
     * that should be annotated.
     *
     * @param definition the message descriptor to scan
     * @return {@code true} if the message descriptor contains fields for annotation
     */
    private boolean shouldAnnotate(Descriptor definition) {
        return definition.getFields()
                         .stream()
                         .anyMatch(this::shouldAnnotate);
    }

    /**
     * Ensures that the specified file descriptor has the expected value
     * for a {@code java_multiple_files} Protobuf option.
     *
     * @param file the file descriptor to check
     * @param expectedValue  the expected value for the {@code java_multiple_files}.
     */
    private static void checkMultipleFilesOption(FileDescriptor file, boolean expectedValue) {
        boolean actualValue = file.getOptions().getJavaMultipleFiles();
        if (actualValue != expectedValue) {
            throw newIllegalStateException("`java_multiple_files` should be `%s`, but was `%s`.",
                                           expectedValue, actualValue);
        }
    }
}
