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

package io.spine.tools.java.code;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.base.SubscribableField;
import io.spine.tools.java.code.field.FieldContainerSpec;
import io.spine.code.java.ClassName;
import io.spine.tools.java.protoc.NestedClass;
import io.spine.tools.java.protoc.NestedClassFactory;
import io.spine.type.MessageType;

import java.util.List;

/**
 * Generates a field enumeration for the given message type.
 *
 * <p>See {@link FieldContainerSpec} for details.
 */
@Immutable
public final class FieldFactory implements NestedClassFactory {

    @Override
    public List<NestedClass> generateClassesFor(MessageType messageType) {
        return createFor(messageType, ClassName.of(SubscribableField.class));
    }

    public List<NestedClass> createFor(MessageType messageType, ClassName fieldSupertype) {
        String generatedCode =
                new FieldContainerSpec(messageType, fieldSupertype)
                        .toPoet()
                        .toString();
        NestedClass result = new NestedClass(generatedCode);
        return ImmutableList.of(result);
    }
}
