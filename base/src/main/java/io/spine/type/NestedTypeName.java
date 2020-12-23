/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.type;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.value.StringTypeValue;

import java.util.Optional;

import static io.spine.type.TypeName.NESTED_TYPE_SEPARATOR;

/**
 * A simple name of a nested Protobuf type.
 *
 * <p>Consists of names of containing types and the name of the type, all separated with dots.
 */
public final class NestedTypeName extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    private static final Joiner simpleNameJoiner = Joiner.on(NESTED_TYPE_SEPARATOR);
    private static final Joiner underscoreNameJoiner = Joiner.on("_");

    private final ImmutableList<String> names;

    private NestedTypeName(ImmutableList<String> names) {
        super(simpleNameJoiner.join(names));
        this.names = names;
    }

    /**
     * Obtains the {@code NestedTypeName} of the given type.
     */
    static NestedTypeName of(Type<?, ?> type) {
        ImmutableList.Builder<String> names = ImmutableList.builder();
        String unqualified = type.descriptor()
                                 .getName();
        names.add(unqualified);
        Optional<Type<Descriptor, DescriptorProto>> parent = type.containingType();
        while (parent.isPresent()) {
            Type<Descriptor, DescriptorProto> containingType = parent.get();
            names.add(containingType.descriptor()
                                    .getName());
            parent = containingType.containingType();
        }
        ImmutableList<String> fullSimpleName = names.build()
                                                    .reverse();
        return new NestedTypeName(fullSimpleName);
    }

    /**
     * Obtains the name joined with underscores ({@code _}), as used in generated code in some
     * languages.
     */
    public String joinWithUnderscore() {
        return underscoreNameJoiner.join(names);
    }
}
