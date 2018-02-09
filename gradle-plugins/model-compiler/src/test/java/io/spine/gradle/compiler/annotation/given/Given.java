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

package io.spine.gradle.compiler.annotation.given;

import com.google.common.base.Function;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.annotation.SPI;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.TypeHolder;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.AnnotationTargetSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.java.JavaCode.toJavaFieldName;
import static io.spine.tools.java.JavaSources.getBuilderClassName;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Dmytro Grankin
 */
public class Given {

    public static final String NO_SPI_OPTIONS_FILENAME = "no_spi_options.proto";
    public static final String NO_SPI_OPTIONS_MULTIPLE_FILENAME = "no_spi_options_multiple.proto";
    private static final Class<? extends Annotation> ANNOTATION_CLASS = SPI.class;

    private Given() {
        // Prevent instantiation of this utility class.
    }

    public static class NestedTypesAnnotationValidator implements SourceValidator {

        private final boolean shouldBeAnnotated;

        public NestedTypesAnnotationValidator(boolean shouldBeAnnotated) {
            this.shouldBeAnnotated = shouldBeAnnotated;
        }

        @Nullable
        @Override
        public Void apply(@Nullable AbstractJavaSource<JavaClassSource> outerClass) {
            checkNotNull(outerClass);
            for (JavaSource<?> nestedType : outerClass.getNestedTypes()) {
                final AnnotationSource annotation = getAnnotation(nestedType);
                if (shouldBeAnnotated) {
                    assertNotNull(annotation);
                } else {
                    assertNull(annotation);
                }
            }
            return null;
        }
    }

    public static class MainDefinitionAnnotationValidator implements SourceValidator {

        private final boolean shouldBeAnnotated;

        public MainDefinitionAnnotationValidator(boolean shouldBeAnnotated) {
            this.shouldBeAnnotated = shouldBeAnnotated;
        }

        @Nullable
        @Override
        public Void apply(@Nullable AbstractJavaSource<JavaClassSource> input) {
            checkNotNull(input);
            final AnnotationSource annotationSource = getAnnotation(input);
            if (shouldBeAnnotated) {
                assertNotNull(annotationSource);
            } else {
                assertNull(annotationSource);
            }
            return null;
        }
    }

    public static class FieldAnnotationValidator implements SourceValidator {

        private final FieldDescriptorProto fieldDescriptor;
        private final boolean shouldBeAnnotated;

        public FieldAnnotationValidator(FieldDescriptorProto fieldDescriptor,
                                        boolean shouldBeAnnotated) {
            this.fieldDescriptor = fieldDescriptor;
            this.shouldBeAnnotated = shouldBeAnnotated;
        }

        @Nullable
        @Override
        public Void apply(@Nullable AbstractJavaSource<JavaClassSource> input) {
            checkNotNull(input);
            final JavaClassSource message = (JavaClassSource) input;
            final JavaClassSource messageBuilder = getBuilder(message);
            checkAccessorsAnnotation(message);
            checkAccessorsAnnotation(messageBuilder);
            return null;
        }

        private void checkAccessorsAnnotation(JavaClassSource message) {
            final String fieldName = toJavaFieldName(fieldDescriptor.getName(), true);
            for (MethodSource method : message.getMethods()) {
                if (method.isPublic() && method.getName().contains(fieldName)) {
                    final AnnotationSource annotation = getAnnotation(method);
                    if (shouldBeAnnotated) {
                        assertNotNull(annotation);
                    } else {
                        assertNull(annotation);
                    }
                }
            }
        }

        private static JavaClassSource getBuilder(JavaSource messageSource) {
            final TypeHolder messageType = (TypeHolder) messageSource;
            final JavaType builderType = messageType.getNestedType(getBuilderClassName());
            return (JavaClassSource) builderType;
        }
    }

    public static class NestedTypeFieldsAnnotationValidator implements SourceValidator {

        private final DescriptorProto messageDescriptor;
        private final boolean shouldBeAnnotated;

        public NestedTypeFieldsAnnotationValidator(DescriptorProto messageDescriptor,
                                                   boolean shouldBeAnnotated) {
            this.messageDescriptor = messageDescriptor;
            this.shouldBeAnnotated = shouldBeAnnotated;
        }

        @SuppressWarnings({
                "ResultOfMethodCallIgnored", // `Void` return type.
                "unchecked"                  // Could not determine exact type for nested declaration.
        })
        @Nullable
        @Override
        public Void apply(@Nullable AbstractJavaSource<JavaClassSource> outerClass) {
            checkNotNull(outerClass);
            for (FieldDescriptorProto fieldDescriptor : messageDescriptor.getFieldList()) {
                final AbstractJavaSource nestedType =
                        (AbstractJavaSource) outerClass.getNestedType(messageDescriptor.getName());
                new FieldAnnotationValidator(fieldDescriptor, shouldBeAnnotated).apply(nestedType);
            }
            return null;
        }
    }

    private static AnnotationSource getAnnotation(AnnotationTargetSource<?, ?> javaSource) {
        for (AnnotationSource annotationSource : javaSource.getAnnotations()) {
            if (annotationSource.getQualifiedName()
                                .equals(ANNOTATION_CLASS.getName())) {
                return annotationSource;
            }
        }
        return null;
    }

    /**
     * Interface for validation of a {@link JavaClassSource}.
     */
    public interface SourceValidator extends Function<AbstractJavaSource<JavaClassSource>, Void> {
    }
}
