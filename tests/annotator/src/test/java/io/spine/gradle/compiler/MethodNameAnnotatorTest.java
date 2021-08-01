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

package io.spine.gradle.compiler;

import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.Message;
import io.spine.test.annotator.Diverse;
import io.spine.test.annotator.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static io.spine.gradle.compiler.given.AnnotatorTestEnv.assertInternal;
import static io.spine.gradle.compiler.given.AnnotatorTestEnv.assertNotInternal;

@DisplayName("`ProtoAnnotatorPlugin` should pick up `internalMethodNames` and")
class MethodNameAnnotatorTest {

    @SuppressWarnings("DuplicateStringLiteralInspection") // Clashes with usages in codegen.
    @Test
    @DisplayName("annotate mentioned methods")
    void annotateMethods() throws NoSuchMethodException {
        Class<? extends Message> messageClass = Diverse.class;
        assertNotInternal(messageClass.getMethod("getText"));

        assertInternal(messageClass.getDeclaredMethod("getSerializedSize"));
        assertInternal(messageClass.getDeclaredMethod("newBuilderForType"));
        assertInternal(messageClass.getDeclaredMethod("parseFrom", byte[].class));
        assertInternal(messageClass.getDeclaredMethod("parseFrom", ByteBuffer.class));
        assertInternal(messageClass.getDeclaredMethod("parseDelimitedFrom", InputStream.class));
        assertInternal(messageClass.getDeclaredMethod("parseDelimitedFrom",
                                                      InputStream.class,
                                                      ExtensionRegistryLite.class));
        assertInternal(Region.class.getDeclaredMethod("internalGetValueMap"));
    }
}
