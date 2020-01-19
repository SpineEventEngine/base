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

package io.spine.code.gen.java;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.code.gen.java.field.FieldsSpec;
import io.spine.tools.protoc.nested.GeneratedNestedClass;
import io.spine.tools.protoc.nested.NestedClassFactory;
import io.spine.type.MessageType;

import java.util.List;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

@Immutable
public final class FieldFactory implements NestedClassFactory {

    private static final String EVENT = "spine.core.Event";
    private static final String EVENT_CONTEXT = "spine.core.EventContext";

    @Override
    public List<GeneratedNestedClass> createFor(MessageType messageType) {
        String generatedCode = FieldsSpec.of(messageType)
                                         .typeSpec(PUBLIC, STATIC, FINAL)
                                         .toString();
        GeneratedNestedClass result = new GeneratedNestedClass(generatedCode);
        return ImmutableList.of(result);
    }

    public static boolean eligibleForFieldsGeneration(MessageType type) {
        return type.isEntityState()
                || type.isEvent()
                || type.isRejection()
                || isEvent(type)
                || isEventContext(type);
    }

    public static boolean isEvent(MessageType type) {
        String typeName = type.name().value();
        return EVENT.equals(typeName);
    }

    public static boolean isEventContext(MessageType type) {
        String typeName = type.name().value();
        return EVENT_CONTEXT.equals(typeName);
    }
}
