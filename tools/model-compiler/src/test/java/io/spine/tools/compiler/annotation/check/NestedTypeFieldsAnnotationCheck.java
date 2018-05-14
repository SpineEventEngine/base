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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Dmytro Grankin
 */
public class NestedTypeFieldsAnnotationCheck implements SourceCheck {

    private final DescriptorProtos.DescriptorProto messageDescriptor;
    private final boolean shouldBeAnnotated;

    public NestedTypeFieldsAnnotationCheck(DescriptorProtos.DescriptorProto messageDescriptor,
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
        for (DescriptorProtos.FieldDescriptorProto fieldDescriptor : messageDescriptor.getFieldList()) {
            final AbstractJavaSource nestedType =
                    (AbstractJavaSource) outerClass.getNestedType(messageDescriptor.getName());
            new FieldAnnotationCheck(fieldDescriptor, shouldBeAnnotated).apply(nestedType);
        }
        return null;
    }
}
