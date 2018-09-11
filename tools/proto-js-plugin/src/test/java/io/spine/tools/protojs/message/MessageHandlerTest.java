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

package io.spine.tools.protojs.message;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.given.Generators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.protojs.given.Generators.assertContains;
import static io.spine.tools.protojs.given.Given.message;
import static io.spine.tools.protojs.message.MessageHandler.FROM_JSON_ARG;
import static io.spine.tools.protojs.message.MessageHandler.FROM_OBJECT_ARG;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("MessageHandler should")
class MessageHandlerTest {

    private Descriptor message;
    private JsGenerator jsGenerator;
    private MessageHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        message = message();
        jsGenerator = new JsGenerator();
        handler = MessageHandler.createFor(message, jsGenerator);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().setDefault(Descriptor.class, message)
                               .testAllPublicStaticMethods(MessageHandler.class);
    }

    @Test
    @DisplayName("generate `fromJson` method for message")
    void generateFromJson() {
        handler.generateFromJsonMethod();
        String methodDeclaration = message.getFullName() + ".fromJson";
        assertGeneratedCodeContains(methodDeclaration);
    }

    @Test
    @DisplayName("parse JSON into JS object in `fromJson` method")
    void parseJsonIntoObject() {
        handler.generateFromJsonMethod();
        String parseStatement = "JSON.parse(" + FROM_JSON_ARG + ')';
        assertGeneratedCodeContains(parseStatement);
    }

    @Test
    @DisplayName("generate `fromObject` method for message")
    void generateFromObject() {
        handler.generateFromObjectMethod();
        String methodDeclaration = message.getFullName() + ".fromObject";
        assertGeneratedCodeContains(methodDeclaration);
    }

    @Test
    @DisplayName("check parsed object for null in `fromObject` method")
    void checkJsObjectForNull() {
        handler.generateFromObjectMethod();
        String check = "if (" + FROM_OBJECT_ARG + " === null) {";
        assertGeneratedCodeContains(check);
    }

    @Test
    @DisplayName("handle message fields in `fromObject` method")
    void handleMessageFields() {
        MessageHandler handler = spy(this.handler);
        handler.generateFromObjectMethod();
        verify(handler, times(1)).handleMessageFields();
    }

    private void assertGeneratedCodeContains(CharSequence toSearch) {
        assertContains(jsGenerator, toSearch);
    }
}
