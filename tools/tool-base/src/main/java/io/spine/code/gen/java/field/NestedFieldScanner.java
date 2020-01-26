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

package io.spine.code.gen.java.field;

import io.spine.code.proto.FieldDeclaration;
import io.spine.type.MessageType;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

final class NestedFieldScanner {

    private final MessageType messageType;

    NestedFieldScanner(MessageType messageType) {
        this.messageType = messageType;
    }

    List<MessageType> scan() {
        List<MessageType> result = newLinkedList();
        int index = -1;
        while (index < result.size()) {
            if (index == -1) {
                addMessageFields(result, this.messageType);
            } else {
                MessageType messageType = result.get(index);
                addMessageFields(result, messageType);
            }
            index++;
        }
        return result;
    }

    private static void addMessageFields(List<MessageType> result, MessageType messageType) {
        messageType.fields()
                   .stream()
                   .filter(FieldSpec::shouldExposeNestedFields)
                   .map(FieldDeclaration::messageType)
                   .filter(type -> !containsTypeWithSameName(result, type))
                   .forEach(result::add);
    }

    private static boolean containsTypeWithSameName(List<MessageType> result, MessageType type) {
        return result.stream()
                     .map(MessageType::simpleJavaClassName)
                     .anyMatch(name -> name.equals(type.simpleJavaClassName()));
    }
}
