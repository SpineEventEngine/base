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

package io.spine.tools.java.compiler.annotation.check;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.code.proto.FieldName;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.TypeHolder;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.java.SimpleClassName.ofBuilder;
import static io.spine.tools.java.compiler.annotation.check.Annotations.findInternalAnnotation;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FieldAnnotationCheck implements SourceCheck {

    private final FieldDescriptor fieldDescriptor;
    private final boolean shouldBeAnnotated;

    public FieldAnnotationCheck(FieldDescriptor fieldDescriptor,
                                boolean shouldBeAnnotated) {
        this.fieldDescriptor = fieldDescriptor;
        this.shouldBeAnnotated = shouldBeAnnotated;
    }

    @Override
    public void accept(@Nullable AbstractJavaSource<JavaClassSource> input) {
        checkNotNull(input);
        JavaClassSource message = (JavaClassSource) input;
        JavaClassSource messageBuilder = getBuilder(message);
        checkAccessorsAnnotation(message);
        checkAccessorsAnnotation(messageBuilder);
    }

    private void checkAccessorsAnnotation(JavaClassSource message) {
        String fieldName = FieldName.of(fieldDescriptor.toProto())
                                    .toCamelCase();
        for (MethodSource method : message.getMethods()) {
            if (method.isPublic() && method.getName()
                                           .contains(fieldName)) {
                Optional<?> annotation = findInternalAnnotation(method);
                if (shouldBeAnnotated) {
                    assertTrue(annotation.isPresent());
                } else {
                    assertFalse(annotation.isPresent());
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
