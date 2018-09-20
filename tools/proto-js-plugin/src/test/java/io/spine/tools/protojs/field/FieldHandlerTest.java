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

import io.spine.tools.protojs.generate.JsOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.tools.protojs.field.FieldGenerator.FIELD_VALUE;
import static io.spine.code.js.Fields.camelCaseName;
import static io.spine.tools.protojs.field.MapFieldGenerator.ATTRIBUTE;
import static io.spine.tools.protojs.field.MapFieldGenerator.MAP_KEY;
import static io.spine.tools.protojs.field.RepeatedFieldGenerator.LIST_ITEM;
import static io.spine.tools.protojs.given.Generators.assertContains;
import static io.spine.tools.protojs.given.Given.mapField;
import static io.spine.tools.protojs.given.Given.repeatedField;
import static io.spine.tools.protojs.given.Given.singularField;
import static io.spine.tools.protojs.message.MessageGenerator.FROM_OBJECT;
import static io.spine.tools.protojs.message.MessageGenerator.FROM_OBJECT_ARG;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dmytro Kuzmin
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
// Generated code duplication needed to check main class.
@DisplayName("FieldHandler should")
class FieldHandlerTest {

    private static final String JS_OBJECT = "jsObject";

    private JsOutput jsOutput;

    private SingularFieldGenerator singularHandler;
    private RepeatedFieldGenerator repeatedHandler;
    private MapFieldGenerator mapHandler;

    @BeforeEach
    void setUp() {
        jsOutput = new JsOutput();
        singularHandler = singularHandler();
        repeatedHandler = repeatedHandler();
        mapHandler = mapHandler();
    }

    @Test
    @DisplayName("acquire field value by field JSON name")
    void acquireJsObject() {
        String fieldValue = singularHandler.acquireFieldValue();
        String expected = FROM_OBJECT_ARG + '.' + singularField().getJsonName();
        assertEquals(expected, fieldValue);
    }

    @Nested
    @DisplayName("iterate")
    class Iterate {

        @Test
        @DisplayName("JS list items in case of repeated field")
        void repeated() {
            repeatedHandler.iterateListValues(JS_OBJECT);
            String forEach = JS_OBJECT + ".forEach";
            assertContains(jsOutput, forEach);
            String forEachItems = '(' + LIST_ITEM + ", index, array)";
            assertContains(jsOutput, forEachItems);
        }

        @Test
        @DisplayName("JS object own properties in case of map field")
        void map() {
            String value = mapHandler.iterateOwnAttributes(JS_OBJECT);
            String iteration = "for (let " + ATTRIBUTE + " in " + JS_OBJECT + ')';
            assertContains(jsOutput, iteration);
            String ownPropertyCheck = "hasOwnProperty(" + ATTRIBUTE + ')';
            assertContains(jsOutput, ownPropertyCheck);
            String expected = JS_OBJECT + '[' + ATTRIBUTE + ']';
            assertEquals(expected, value);
        }
    }

    @Test
    @DisplayName("call field value precondition to check field value for null")
    void callChecker() {
        String fieldValue = singularHandler.acquireFieldValue();
        singularHandler.generateJs();
        String nullCheck = "if (" + fieldValue + " === null)";
        assertContains(jsOutput, nullCheck);
    }

    @Test
    @DisplayName("call field value parser to parse field value")
    void callParser() {
        String fieldValue = singularHandler.acquireFieldValue();
        singularHandler.generateJs();
        String typeName = singularField().getMessageType()
                                         .getFullName();
        String recursiveCall = typeName + '.' + FROM_OBJECT + '(' + fieldValue + ')';
        assertContains(jsOutput, recursiveCall);
    }

    @Test
    @DisplayName("parse object attribute value to obtain key in case of map field")
    void parseMapKey() {
        mapHandler.generateJs();
        String parseAttribute = MAP_KEY + " = parseInt(" + ATTRIBUTE + ')';
        assertContains(jsOutput, parseAttribute);
    }

    @Test
    @DisplayName("set singular field")
    void setSingular() {
        singularHandler.generateJs();
        String setterCall = "set" + camelCaseName(singularField()) + '(' + FIELD_VALUE + ')';
        assertContains(jsOutput, setterCall);
    }

    @Test
    @DisplayName("add value to repeated field")
    void addToRepeated() {
        repeatedHandler.generateJs();
        String addCall = "add" + camelCaseName(repeatedField()) + '(' + FIELD_VALUE + ')';
        assertContains(jsOutput, addCall);
    }

    @Test
    @DisplayName("add value to map field")
    void addToMap() {
        mapHandler.generateJs();
        String getMapCall = "get" + camelCaseName(mapField()) + "Map()";
        String addToMapCall = "set(" + MAP_KEY + ", " + FIELD_VALUE + ')';
        String addCall = getMapCall + '.' + addToMapCall;
        assertContains(jsOutput, addCall);
    }

    private SingularFieldGenerator singularHandler() {
        return (SingularFieldGenerator) FieldGenerators.createFor(singularField(), jsOutput);
    }

    private RepeatedFieldGenerator repeatedHandler() {
        return (RepeatedFieldGenerator) FieldGenerators.createFor(repeatedField(), jsOutput);
    }

    private MapFieldGenerator mapHandler() {
        return (MapFieldGenerator) FieldGenerators.createFor(mapField(), jsOutput);
    }
}
