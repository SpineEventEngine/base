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

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.FieldHolderSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.gradle.compiler.annotation.TypeDefinitionAnnotator.getNestedTypeByName;
import static io.spine.gradle.compiler.util.JavaCode.toJavaFieldName;
import static io.spine.gradle.compiler.util.JavaSources.getBuilderClassName;
import static io.spine.gradle.compiler.util.JavaSources.getFilePath;
import static io.spine.gradle.compiler.util.UnknownOptions.getUnknownOptionValue;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.String.format;

/**
 * A field annotator.
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
    void annotate() {
        for (FileDescriptorProto fileDescriptor : fileDescriptors) {
            annotate(fileDescriptor);
        }
    }

    @Override
    protected void annotateSingularFile(FileDescriptorProto fileDescriptor) {
        if (!shouldAnnotate(fileDescriptor)) {
            return;
        }

        final Path filePath = getFilePath(fileDescriptor);
        rewriteSource(filePath, new FileFieldAnnotation<JavaClassSource>(fileDescriptor));
    }

    @Override
    protected void annotateMultipleFiles(FileDescriptorProto fileDescriptor) {
        for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
            if (shouldAnnotate(messageDescriptor)) {
                final Path filePath = getFilePath(messageDescriptor, false,
                                                  fileDescriptor);
                rewriteSource(filePath,
                              new MessageFieldAnnotation<JavaClassSource>(fileDescriptor,
                                                                          messageDescriptor));
            }
        }
    }

    @Override
    protected String getRawOptionValue(FieldDescriptorProto descriptor) {
        return getUnknownOptionValue(descriptor, getOptionNumber());
    }

    @VisibleForTesting
    static JavaClassSource getBuilder(JavaSource messageSource) {
        final JavaClassSource messageClass = asClassSource(messageSource);
        final JavaSource builderSource = messageClass.getNestedType(getBuilderClassName());
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

        private FileFieldAnnotation(FileDescriptorProto fileDescriptor) {
            checkMultipleFilesOption(fileDescriptor, false);
            this.fileDescriptor = fileDescriptor;
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
            for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
                final Iterable<String> unannotatableFields = getUnannotatableFields(messageDescriptor);
                processMessageDescriptor(input, messageDescriptor, unannotatableFields);
            }
            return null;
        }

        private void processMessageDescriptor(AbstractJavaSource<T> input,
                                              DescriptorProto messageDescriptor,
                                              Iterable<String> unannotatableFields) {
            for (FieldDescriptorProto fieldDescriptor : messageDescriptor.getFieldList()) {
                if (shouldAnnotate(fieldDescriptor)) {
                    final JavaSource message = getNestedTypeByName(input,
                                                                   messageDescriptor.getName());
                    annotateMessageField(asClassSource(message), fieldDescriptor, unannotatableFields);
                }
            }
        }
    }

    /**
     * An annotation function for a {@link #messageDescriptor}.
     *
     * @param <T> the type of a Java source, that may contains field definitions
     */
    private class MessageFieldAnnotation<T extends JavaSource<T> & FieldHolderSource<T>>
            implements SourceVisitor<T> {

        /**
         * A message descriptor for a file descriptor,
         * that has {@code true} value for a {@code java_multiple_files} option.
         */
        private final DescriptorProto messageDescriptor;

        private MessageFieldAnnotation(FileDescriptorProto fileDescriptor,
                                       DescriptorProto messageDescriptor) {
            if (!fileDescriptor.getMessageTypeList()
                               .contains(messageDescriptor)) {
                throw newIllegalStateException(
                        "Specified message descriptor `%s` does not belong to file descriptor `%s`.",
                        messageDescriptor, fileDescriptor);
            }
            checkMultipleFilesOption(fileDescriptor, true);

            this.messageDescriptor = messageDescriptor;
        }

        /**
         * Annotates the accessors, which should be annotated, within the specified input.
         *
         * @param input the {@link AbstractJavaSource} for the {@link #messageDescriptor}
         * @return {@code Void}
         */
        @Nullable
        @Override
        public Void apply(@Nullable AbstractJavaSource<T> input) {
            checkNotNull(input);
            final Iterable<String> unannotatableFields = getUnannotatableFields(messageDescriptor);
            for (FieldDescriptorProto fieldDescriptor : messageDescriptor.getFieldList()) {
                if (shouldAnnotate(fieldDescriptor)) {
                    annotateMessageField(asClassSource(input), fieldDescriptor, unannotatableFields);
                }
            }
            return null;
        }
    }

    /**
     * Annotates the accessors for the specified field.
     *
     * @param message             the message, that contains field for annotation
     * @param fieldDescriptor     the field descriptor to get field name
     * @param unannotatableFields the field names that should not be annotated
     */
    private void annotateMessageField(JavaClassSource message,
                                      FieldDescriptorProto fieldDescriptor,
                                      Iterable<String> unannotatableFields) {
        final String capitalizedFieldName = toJavaFieldName(fieldDescriptor.getName(), true);
        final JavaClassSource messageBuilder = getBuilder(message);

        annotateAccessors(message, capitalizedFieldName, unannotatableFields);
        annotateAccessors(messageBuilder, capitalizedFieldName, unannotatableFields);
    }

    /**
     * Annotates {@code public} accessors for the specified field.
     *
     * @param classSource          class source to modify
     * @param capitalizedFieldName the field name to get accessors
     * @param unannotatedFields    the field names that should not be annotated
     */
    private void annotateAccessors(JavaClassSource classSource, String capitalizedFieldName,
                                   Iterable<String> unannotatedFields) {
        for (MethodSource method : classSource.getMethods()) {
            final boolean shouldAnnotate =
                    shouldAnnotateMethod(method.getName(), capitalizedFieldName, unannotatedFields);
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
    static boolean shouldAnnotateMethod(String methodName, String capitalizedFieldName,
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
    private Iterable<String> getUnannotatableFields(DescriptorProto messageDescriptor) {
        final Collection<String> fieldNames = newLinkedList();
        for (FieldDescriptorProto fieldDescriptor : messageDescriptor.getFieldList()) {
            if (shouldAnnotate(fieldDescriptor)) {
                final String capitalizedFieldName = toJavaFieldName(fieldDescriptor.getName(),
                                                                    true);
                fieldNames.add(capitalizedFieldName);
            }
        }
        return fieldNames;
    }

    /**
     * Tells whether the specified file descriptor contains at least
     * a message descriptor with at least a field, that should be annotated.
     *
     * @param fileDescriptor the file descriptor to scan
     * @return {@code true} if the file descriptor contains fields for annotation
     */
    private boolean shouldAnnotate(FileDescriptorProto fileDescriptor) {
        for (DescriptorProto messageDescriptor : fileDescriptor.getMessageTypeList()) {
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
     * @param messageDescriptor the message descriptor to scan
     * @return {@code true} if the message descriptor contains fields for annotation
     */
    private boolean shouldAnnotate(DescriptorProto messageDescriptor) {
        for (FieldDescriptorProto fieldDescriptor : messageDescriptor.getFieldList()) {
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
     * @param fileDescriptor the file descriptor to check
     * @param expectedValue  the expected value for the {@code java_multiple_files}.
     */
    private static void checkMultipleFilesOption(FileDescriptorProto fileDescriptor,
                                                 boolean expectedValue) {
        final boolean actualValue = fileDescriptor.getOptions()
                                                  .hasJavaMultipleFiles();
        if (actualValue != expectedValue) {
            throw newIllegalStateException("`java_multiple_files` should be `%s`, but was `%s`.",
                                           expectedValue, actualValue);
        }
    }
}
