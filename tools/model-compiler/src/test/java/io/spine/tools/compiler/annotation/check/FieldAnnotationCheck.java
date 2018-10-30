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

package io.spine.tools.compiler.annotation.check;

import com.google.protobuf.DescriptorProtos;
import io.spine.code.proto.FieldName;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.TypeHolder;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.java.SimpleClassName.ofBuilder;
import static io.spine.tools.compiler.annotation.check.Annotations.findSpiAnnotation;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FieldAnnotationCheck implements SourceCheck {

    private final DescriptorProtos.FieldDescriptorProto fieldDescriptor;
    private final boolean shouldBeAnnotated;

    public FieldAnnotationCheck(DescriptorProtos.FieldDescriptorProto fieldDescriptor,
                                boolean shouldBeAnnotated) {
        this.fieldDescriptor = fieldDescriptor;
        this.shouldBeAnnotated = shouldBeAnnotated;
    }

    @Override
    public @Nullable Void apply(@Nullable AbstractJavaSource<JavaClassSource> input) {
        checkNotNull(input);
        JavaClassSource message = (JavaClassSource) input;
        JavaClassSource messageBuilder = getBuilder(message);
        checkAccessorsAnnotation(message);
        checkAccessorsAnnotation(messageBuilder);
        return null;
    }

    private void checkAccessorsAnnotation(JavaClassSource message) {
        String fieldName = FieldName.of(fieldDescriptor)
                                    .toCamelCase();
        for (MethodSource method : message.getMethods()) {
            if (method.isPublic() && method.getName()
                                           .contains(fieldName)) {
                AnnotationSource annotation = findSpiAnnotation(method);
                if (shouldBeAnnotated) {
                    assertNotNull(annotation);
                } else {
                    assertNull(annotation);
                }
            }
        }
    }

    private static JavaClassSource getBuilder(JavaSource messageSource) {
        TypeHolder messageType = (TypeHolder) messageSource;
        JavaType builderType = messageType.getNestedType(ofBuilder().value());
        return (JavaClassSource) builderType;
    }
}
