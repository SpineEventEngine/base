/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.code.gen.java;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.code.gen.java.field.EntityStateFields;
import io.spine.code.gen.java.field.EventMessageFields;
import io.spine.code.gen.java.field.FieldsSpec;
import io.spine.tools.protoc.nested.GeneratedNestedClass;
import io.spine.tools.protoc.nested.NestedClassFactory;
import io.spine.type.MessageType;

import java.util.List;

/**
 * Generates a field enumeration for the given message type.
 *
 * <p>See {@link FieldsSpec} for details.
 */
@Immutable
public final class FieldFactory implements NestedClassFactory {

    @Override
    public List<GeneratedNestedClass> createFor(MessageType messageType) {
        if (messageType.isEntityState()) {
            return generateEntityStateFields(messageType);
        }
        if (messageType.isEvent() || messageType.isRejection()) {
            return generateEventMessageFields(messageType);
        }
        return ImmutableList.of();
    }

    private static List<GeneratedNestedClass> generateEntityStateFields(MessageType type) {
        GeneratedNestedClass nestedClass = generatedNestedClass(new EntityStateFields(type));
        return ImmutableList.of(nestedClass);
    }

    private static List<GeneratedNestedClass> generateEventMessageFields(MessageType type) {
        GeneratedNestedClass nestedClass = generatedNestedClass(new EventMessageFields(type));
        return ImmutableList.of(nestedClass);
    }

    private static GeneratedNestedClass generatedNestedClass(GeneratedTypeSpec spec) {
        String generatedCode = spec.typeSpec()
                                   .toString();
        return new GeneratedNestedClass(generatedCode);
    }
}
