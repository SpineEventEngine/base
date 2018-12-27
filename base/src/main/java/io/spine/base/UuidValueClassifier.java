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

package io.spine.base;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;

/**
 * Checks if the given message definition is a {@link UuidValue}.
 */
@Immutable
final class UuidValueClassifier extends MessageClassifier {

    static final String FIELD_NAME = "uuid";

    @Override
    public boolean doTest(DescriptorProto message, FileDescriptorProto declaringFile) {
        return doTest(message);
    }

    boolean doTest(DescriptorProto message) {
        int fieldCount = message.getFieldCount();
        if (fieldCount != 1) {
            return false;
        }
        FieldDescriptorProto theField = message.getFieldList()
                                               .get(0);
        boolean nameMatches = theField.getName()
                                      .equals(FIELD_NAME);
        boolean typeMatches = theField.getType() == TYPE_STRING;
        return nameMatches && typeMatches;
    }
}
