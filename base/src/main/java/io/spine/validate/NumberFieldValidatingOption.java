/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.validate;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors;
import io.spine.code.proto.FieldDeclaration;

import java.util.Set;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.DOUBLE;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.FLOAT;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.INT;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.LONG;

public abstract class NumberFieldValidatingOption<T, V extends Number> extends FieldValidatingOption<T, V> {

    private static final Set<Descriptors.FieldDescriptor.JavaType> MAX_OPTION_APPLICABLE = ImmutableSet.of(
            LONG, INT, FLOAT, DOUBLE
    );

    @Override
    boolean applicableTo(FieldDeclaration field) {
        return MAX_OPTION_APPLICABLE.contains(field.javaType());
    }
}
