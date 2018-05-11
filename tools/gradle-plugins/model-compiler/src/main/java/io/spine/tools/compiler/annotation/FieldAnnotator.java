/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.tools.java.SimpleClassName;
import io.spine.tools.java.SourceFile;
import io.spine.tools.proto.FieldName;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.FieldHolderSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.lang.annotation.Annotation;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.option.UnknownOptions.get;
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
class FieldAnnotator extends Annotator<FieldOptions, FieldDescriptorProto> {

    FieldAnnotator(Class<? extends Annotation> annotation,
                   GeneratedExtension<FieldOptions, Boolean> option,
                   Collection<FileDescriptorProto> fileDescriptors,
                   String genProtoDir) {
        super(annotation, option, fileDescriptors, genProtoDir);
    }

    @Override
    public void annotate() {
        for (FileDescriptorProto file : fileDescriptors()) {
            annotate(file);
        }
    }

    @Override
    protected void annotateOneFile(FileDescriptorProto file) {
        if (!shouldAnnotate(file)) {
            return;
        }

        final SourceFile outerClass = SourceFile.forOuterClassOf(file);
        rewriteSource(outerClass, new FileFieldAnnotation<JavaClassSource>(file));
    }

    @Override
    protected void annotateMultipleFiles(FileDescriptorProto file) {
        for (DescriptorProto messageType : file.getMessageTypeList()) {
            if (shouldAnnotate(messageType)) {
                final SourceVisitor<JavaClassSource> annotation =
                        new MessageFieldAnnotation<>(file, messageType);
                final SourceFile filePath = SourceFile.forMessage(messageType, false, file);
                rewriteSource(filePath, annotation);
            }
        }
    }

    @Override
    protected String getRawOptionValue(FieldDescriptorProto field) {
        return get(field, getOptionNumber());
    }

    @VisibleForTesting
    static JavaClassSource getBuilder(JavaSource messageSource) {
        final JavaClassSource messageClass = asClassSource(messageSource);
        final JavaSource builderSource = messageClass.getNestedType(SimpleClassName.ofBuilder()
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
            final String errMsg = format("`%s expected to be a class.",
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
        private final FileDescriptorProto fileDescriptor;

        private FileFieldAnnotation(FileDescriptorProto file) {
            checkMultipleFilesOption(file, false);
            this.fileDescriptor = file;
        }

        /**
         * Annotates the accessors, which should be annotated, within the specified input.
         *
         * @param input the {@link AbstractJavaSource} for the {@link #fileDescriptor}
         * @return {@code Void}
         */
        @Nullable
        @Override
        public Void apply(@Nullable AbstractJavaSource<T> input) {
            checkNotNull(input);
            for (DescriptorProto messageType : fileDescriptor.getMessageTypeList()) {
                final Iterable<String> unannotatableFields = getNotAnnotatableFields(messageType);
                processMessageDescriptor(input, messageType, unannotatableFields);
            }
            return null;
        }

        private void processMessageDescriptor(AbstractJavaSource<T> input,
                                              DescriptorProto messageType,
                                              Iterable<String> unannotatableFields) {
            for (FieldDescriptorProto field : messageType.getFieldList()) {
                if (shouldAnnotate(field)) {
                    final JavaSource message = findNestedType(input, messageType.getName());
                    annotateMessageField(asClassSource(message), field, unannotatableFields);
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
        private final DescriptorProto message;

        private MessageFieldAnnotation(FileDescriptorProto file, DescriptorProto message) {
            if (!file.getMessageTypeList()
                               .contains(message)) {
                throw newIllegalStateException(
                        "Specified message `%s` does not belong to the file `%s`.",
                        message, file);
            }
            checkMultipleFilesOption(file, true);

            this.message = message;
        }

        /**
         * Annotates the accessors, which should be annotated, within the specified input.
         *
         * @param input the {@link AbstractJavaSource} for the {@link #message}
         * @return {@code Void}
         */
        @Nullable
        @Override
        public Void apply(@Nullable AbstractJavaSource<T> input) {
            checkNotNull(input);
            final Iterable<String> fieldsToSkip = getNotAnnotatableFields(message);
            for (FieldDescriptorProto field : message.getFieldList()) {
                if (shouldAnnotate(field)) {
                    annotateMessageField(asClassSource(input), field, fieldsToSkip);
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
     * @param skipFields
     *        the field names that should not be annotated
     */
    private void annotateMessageField(JavaClassSource message,
                                      FieldDescriptorProto field,
                                      Iterable<String> skipFields) {
        final String capitalizedFieldName = FieldName.of(field)
                                                     .toCamelCase();
        final JavaClassSource messageBuilder = getBuilder(message);

        annotateAccessors(message, capitalizedFieldName, skipFields);
        annotateAccessors(messageBuilder, capitalizedFieldName, skipFields);
    }

    /**
     * Annotates {@code public} accessors for the specified field.
     *
     * @param javaSource
     *        class source to modify
     * @param capitalizedFieldName
     *        the field name to get accessors
     * @param skipFields
     *        the field names that should not be annotated
     */
    private void annotateAccessors(JavaClassSource javaSource,
                                   String capitalizedFieldName,
                                   Iterable<String> skipFields) {
        for (MethodSource method : javaSource.getMethods()) {
            final boolean shouldAnnotate =
                    shouldAnnotateMethod(method.getName(), capitalizedFieldName, skipFields);
            if (method.isPublic() && shouldAnnotate) {
                addAnnotation(method);
            }
        }
    }

    /**
     * Tells whether a method with the specified name relates to the specified field name.
     *
     * @param methodName           the method name to check annotation need
     * @param capitalizedFieldName the field name, that requires annotation
     * @param unannotatableFields  the capitalized names of fields, that do not require annotation
     * @return {@code true} if a method with the specified name should be annotated,
     *         {@code false} otherwise
     */
    @VisibleForTesting
    static boolean shouldAnnotateMethod(String methodName,
                                        String capitalizedFieldName,
                                        Iterable<String> unannotatableFields) {
        if (!methodName.contains(capitalizedFieldName)) {
            return false;
        }

        for (String unannotatableField : unannotatableFields) {
            final boolean isDebatableMethod =
                    unannotatableField.length() > capitalizedFieldName.length();
            if (isDebatableMethod && methodName.contains(unannotatableField)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Obtains the capitalized field names, that should not be annotated,
     * for the specified message descriptor.
     *
     * @param messageDescriptor the message descriptor to collect the fields
     * @return the capitalized field names
     * @see #shouldAnnotate(com.google.protobuf.GeneratedMessageV3)
     */
    private Iterable<String> getNotAnnotatableFields(DescriptorProto messageDescriptor) {
        final Collection<String> fieldNames = newLinkedList();
        for (FieldDescriptorProto fieldDescriptor : messageDescriptor.getFieldList()) {
            if (shouldAnnotate(fieldDescriptor)) {
                final String capitalizedFieldName = FieldName.of(fieldDescriptor)
                                                             .toCamelCase();
                fieldNames.add(capitalizedFieldName);
            }
        }
        return fieldNames;
    }

    /**
     * Tells whether the specified file descriptor contains at least
     * a message descriptor with at least a field, that should be annotated.
     *
     * @param file the file descriptor to scan
     * @return {@code true} if the file descriptor contains fields for annotation
     */
    private boolean shouldAnnotate(FileDescriptorProto file) {
        for (DescriptorProto messageDescriptor : file.getMessageTypeList()) {
            if (shouldAnnotate(messageDescriptor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether the specified message descriptor contains at least a field,
     * that should be annotated.
     *
     * @param definition the message descriptor to scan
     * @return {@code true} if the message descriptor contains fields for annotation
     */
    private boolean shouldAnnotate(DescriptorProto definition) {
        for (FieldDescriptorProto fieldDescriptor : definition.getFieldList()) {
            if (shouldAnnotate(fieldDescriptor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ensures that the specified file descriptor has the expected value
     * for a {@code java_multiple_files} Protobuf option.
     *
     * @param file the file descriptor to check
     * @param expectedValue  the expected value for the {@code java_multiple_files}.
     */
    private static void checkMultipleFilesOption(FileDescriptorProto file, boolean expectedValue) {
        final boolean actualValue = file.getOptions()
                                        .hasJavaMultipleFiles();
        if (actualValue != expectedValue) {
            throw newIllegalStateException("`java_multiple_files` should be `%s`, but was `%s`.",
                                           expectedValue, actualValue);
        }
    }
}
