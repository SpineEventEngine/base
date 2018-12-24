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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.code.java.GeneratedAccessors;
import io.spine.code.java.SimpleClassName;
import io.spine.code.java.SourceFile;
import io.spine.code.proto.FieldDeclaration;
import io.spine.option.Options;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.FieldHolderSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.compiler.annotation.TypeDefinitionAnnotator.findNestedType;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.String.format;

/**
 * Annotates field accessor in a generated Java code.
 *
 * <p>Annotates {@code public} accessors for a field in a generated Java source
 * if a specified {@linkplain FieldOptions field option} value is {@code true}
 * for the field definition.
 *
 * @author Dmytro Grankin
 */
class FieldAnnotator extends Annotator<FieldOptions, FieldDescriptor> {

    FieldAnnotator(Class<? extends Annotation> annotation,
                   GeneratedExtension<FieldOptions, Boolean> option,
                   Collection<FileDescriptor> fileDescriptors,
                   String genProtoDir) {
        super(annotation, option, fileDescriptors, genProtoDir);
    }

    @Override
    public void annotate() {
        for (FileDescriptor file : fileDescriptors()) {
            annotate(file);
        }
    }

    @Override
    protected void annotateOneFile(FileDescriptor file) {
        if (!shouldAnnotate(file)) {
            return;
        }

        SourceFile outerClass = SourceFile.forOuterClassOf(file.toProto());
        rewriteSource(outerClass, new FileFieldAnnotation<JavaClassSource>(file));
    }

    @Override
    protected void annotateMultipleFiles(FileDescriptor file) {
        for (Descriptor messageType : file.getMessageTypes()) {
            if (shouldAnnotate(messageType)) {
                SourceVisitor<JavaClassSource> annotation =
                        new MessageFieldAnnotation<>(messageType);
                SourceFile filePath = SourceFile.forMessage(messageType.toProto(), file.toProto());
                rewriteSource(filePath, annotation);
            }
        }
    }

    @Override
    protected Optional<Boolean> getOptionValue(FieldDescriptor file) {
        return Options.option(file, getOption());
    }

    @VisibleForTesting
    static JavaClassSource getBuilder(JavaSource messageSource) {
        JavaClassSource messageClass = asClassSource(messageSource);
        JavaSource builderSource = messageClass.getNestedType(SimpleClassName.ofBuilder()
                                                                             .value());
        return asClassSource(builderSource);
    }

    /**
     * Casts a {@link JavaType} to a {@link JavaClassSource}.
     *
     * @param javaType the type to cast
     * @return a casted instance
     * @throws IllegalStateException if the specified source is not a class
     */
    private static JavaClassSource asClassSource(JavaType<?> javaType) {
        if (!javaType.isClass()) {
            String errMsg = format("`%s expected to be a class.",
                                   javaType.getQualifiedName());
            throw new IllegalStateException(errMsg);
        }

        return (JavaClassSource) javaType;
    }

    /**
     * An annotation function for the {@link #fileDescriptor}.
     *
     * @param <T> the type of a Java source, that may contains field definitions
     */
    private class FileFieldAnnotation<T extends JavaSource<T> & FieldHolderSource<T>>
            implements SourceVisitor<T> {

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
         * @return {@code Void}
         */
        @Override
        public @Nullable Void apply(@Nullable AbstractJavaSource<T> input) {
            checkNotNull(input);
            for (Descriptor messageType : fileDescriptor.getMessageTypes()) {
                processMessageDescriptor(input, messageType);
            }
            return null;
        }

        private void processMessageDescriptor(AbstractJavaSource<T> input,
                                              Descriptor messageType) {
            for (FieldDescriptor field : messageType.getFields()) {
                if (shouldAnnotate(field)) {
                    JavaSource message = findNestedType(input, messageType.getName());
                    annotateMessageField(asClassSource(message), new FieldDeclaration(field));
                }
            }
        }
    }

    /**
     * An annotation function for a {@link #message}.
     *
     * @param <T> the type of a Java source, that may contains field definitions
     */
    private class MessageFieldAnnotation<T extends JavaSource<T> & FieldHolderSource<T>>
            implements SourceVisitor<T> {

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
         * @return {@code Void}
         */
        @Override
        public @Nullable Void apply(@Nullable AbstractJavaSource<T> input) {
            checkNotNull(input);
            for (FieldDescriptor field : message.getFields()) {
                if (shouldAnnotate(field)) {
                    annotateMessageField(asClassSource(input), new FieldDeclaration(field));
                }
            }
            return null;
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
        JavaClassSource messageBuilder = getBuilder(message);
        annotateAccessors(message, field);
        annotateAccessors(messageBuilder, field);
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
        ImmutableSet<String> names = GeneratedAccessors.forField(field)
                                                       .names();
        try (PrintStream out = new PrintStream(new File("/Users/ddashenkov/Desktop/build.log"))) {
            out.println("Lookup started");
            javaSource.getMethods()
                      .stream()
                      .peek(method -> out.println("Checking method " + method))
                      .filter(MethodSource::isPublic)
                      .filter(method -> names.contains(method.getName()))
                      .peek(method -> out.println("Marking method " + method))
                      .forEach(this::addAnnotation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
