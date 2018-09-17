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

package io.spine.tools.protojs.field;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.testing.UtilityClassTest;
import io.spine.tools.protojs.generate.JsOutput;
import io.spine.tools.protojs.field.checker.MessageFieldPrecondition;
import io.spine.tools.protojs.field.checker.PrimitiveFieldPrecondition;
import io.spine.tools.protojs.field.parser.EnumFieldParser;
import io.spine.tools.protojs.field.parser.MessageFieldParser;
import io.spine.tools.protojs.field.parser.PrimitiveFieldParser;
import io.spine.tools.protojs.field.parser.WellKnownFieldParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Verify.assertInstanceOf;
import static io.spine.tools.protojs.given.Given.enumField;
import static io.spine.tools.protojs.given.Given.mapField;
import static io.spine.tools.protojs.given.Given.messageField;
import static io.spine.tools.protojs.given.Given.primitiveField;
import static io.spine.tools.protojs.given.Given.repeatedField;
import static io.spine.tools.protojs.given.Given.timestampField;

/**
 * @author Dmytro Kuzmin
 */
@DisplayName("FieldHandlers utility should")
class FieldHandlersTest extends UtilityClassTest<FieldHandlers> {

    private JsOutput jsOutput;

    FieldHandlersTest() {
        super(FieldHandlers.class);
    }

    @Override
    protected void setDefaults(NullPointerTester tester) {
        tester.setDefault(FieldDescriptor.class, messageField());
    }

    @BeforeEach
    void setUp() {
        jsOutput = new JsOutput();
    }

    @Test
    @DisplayName("create singular handler for ordinary Protobuf field")
    void createSingularHandler() {
        FieldHandler handler = handlerFor(messageField());
        assertInstanceOf(SingularFieldHandler.class, handler);
    }

    @Test
    @DisplayName("create repeated handler for repeated Protobuf field")
    void createRepeatedHandler() {
        FieldHandler handler = handlerFor(repeatedField());
        assertInstanceOf(RepeatedFieldHandler.class, handler);
    }

    @Test
    @DisplayName("create map handler for map Protobuf field")
    void createMapHandler() {
        FieldHandler handler = handlerFor(mapField());
        assertInstanceOf(MapFieldHandler.class, handler);
    }

    @Test
    @DisplayName("set value checker of correct type for handler")
    void setValueChecker() {
        AbstractFieldHandler messageHandler = handlerFor(messageField());
        assertInstanceOf(MessageFieldPrecondition.class, messageHandler.checker());

        AbstractFieldHandler primitiveHandler = handlerFor(primitiveField());
        assertInstanceOf(PrimitiveFieldPrecondition.class, primitiveHandler.checker());
    }

    @Test
    @DisplayName("set value parser of correct type for handler")
    void setValueParser() {
        AbstractFieldHandler primitiveHandler = handlerFor(primitiveField());
        assertInstanceOf(PrimitiveFieldParser.class, primitiveHandler.parser());

        AbstractFieldHandler enumHandler = handlerFor(enumField());
        assertInstanceOf(EnumFieldParser.class, enumHandler.parser());

        AbstractFieldHandler messageHandler = handlerFor(messageField());
        assertInstanceOf(MessageFieldParser.class, messageHandler.parser());

        AbstractFieldHandler timestampHandler = handlerFor(timestampField());
        assertInstanceOf(WellKnownFieldParser.class, timestampHandler.parser());
    }

    @Test
    @DisplayName("create value parser for key and value in case of map field")
    void setParsersForMapField() {
        MapFieldHandler handler = (MapFieldHandler) handlerFor(mapField());
        assertInstanceOf(PrimitiveFieldParser.class, handler.keyParser());
        assertInstanceOf(MessageFieldParser.class, handler.parser());
    }

    private AbstractFieldHandler handlerFor(FieldDescriptor field) {
        return (AbstractFieldHandler) FieldHandlers.createFor(field, jsOutput);
    }
}
