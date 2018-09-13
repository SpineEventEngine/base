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

import io.spine.tools.protojs.code.JsOutput;
import io.spine.tools.protojs.given.Generators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.tools.protojs.field.AbstractFieldHandler.FIELD_VALUE;
import static io.spine.tools.protojs.field.Fields.capitalizedName;
import static io.spine.tools.protojs.field.MapFieldHandler.ATTRIBUTE;
import static io.spine.tools.protojs.field.MapFieldHandler.MAP_KEY;
import static io.spine.tools.protojs.field.RepeatedFieldHandler.LIST_ITEM;
import static io.spine.tools.protojs.given.Given.mapField;
import static io.spine.tools.protojs.given.Given.repeatedField;
import static io.spine.tools.protojs.given.Given.singularField;
import static io.spine.tools.protojs.message.MessageHandler.FROM_OBJECT_ARG;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dmytro Kuzmin
 */
@DisplayName("FieldHandler should")
class FieldHandlerTest {

    private static final String JS_OBJECT = "jsObject";

    private JsOutput jsOutput;

    private SingularFieldHandler singularHandler;
    private RepeatedFieldHandler repeatedHandler;
    private MapFieldHandler mapHandler;

    @BeforeEach
    void setUp() {
        jsOutput = new JsOutput();
        singularHandler = singularHandler();
        repeatedHandler = repeatedHandler();
        mapHandler = mapHandler();
    }

    @Test
    @DisplayName("acquire JS object by field JSON name")
    void acquireJsObject() {
        String jsObject = singularHandler.acquireJsObject();
        String expected = FROM_OBJECT_ARG + '.' + singularField().getJsonName();
        assertEquals(expected, jsObject);
    }

    @Nested
    @DisplayName("iterate")
    class Iterate {

        @Test
        @DisplayName("JS list items in case of repeated field")
        void repeated() {
            repeatedHandler.iterateListValues(JS_OBJECT);
            String forEach = JS_OBJECT + ".forEach";
            assertGeneratedCodeContains(forEach);
            String forEachVariables = '(' + LIST_ITEM + ", index, array)";
            assertGeneratedCodeContains(forEachVariables);
        }

        @Test
        @DisplayName("JS object own properties in case of map field")
        void map() {
            String value = mapHandler.iterateOwnAttributes(JS_OBJECT);
            String iteration = "for (let " + ATTRIBUTE + " in " + JS_OBJECT + ')';
            assertGeneratedCodeContains(iteration);
            String ownPropertyCheck = "hasOwnProperty(" + ATTRIBUTE + ')';
            assertGeneratedCodeContains(ownPropertyCheck);
            String expected = JS_OBJECT + '[' + ATTRIBUTE + ']';
            assertEquals(expected, value);
        }
    }

    @Test
    @DisplayName("call field value checker to check field value for null")
    void callChecker() {
        String jsObject = singularHandler.acquireJsObject();
        singularHandler.generateJs();
        String nullCheck = "if (" + jsObject + " === null)";
        assertGeneratedCodeContains(nullCheck);
    }

    @Test
    @DisplayName("call field value parser to parse field value")
    void callParser() {
        String jsObject = singularHandler.acquireJsObject();
        singularHandler.generateJs();
        String typeName = singularField().getMessageType()
                                         .getFullName();
        String recursiveCall = typeName + ".fromObject(" + jsObject + ')';
        assertGeneratedCodeContains(recursiveCall);
    }

    @Test
    @DisplayName("parse object attribute value to obtain key in case of map field")
    void parseMapKey() {
        mapHandler.generateJs();
        String parseAttribute = MAP_KEY + " = parseInt(" + ATTRIBUTE + ')';
        assertGeneratedCodeContains(parseAttribute);
    }

    @Test
    @DisplayName("set singular field")
    void setSingular() {
        singularHandler.generateJs();
        String setterCall = "set" + capitalizedName(singularField()) + '(' + FIELD_VALUE + ')';
        assertGeneratedCodeContains(setterCall);
    }

    @Test
    @DisplayName("add value to repeated field")
    void addToRepeated() {
        repeatedHandler.generateJs();
        String addCall = "add" + capitalizedName(repeatedField()) + '(' + FIELD_VALUE + ')';
        assertGeneratedCodeContains(addCall);
    }

    @Test
    @DisplayName("add value to map field")
    void addToMap() {
        mapHandler.generateJs();
        String getMapCall = "get" + capitalizedName(mapField()) + "Map()";
        String addToMapCall = "set(" + MAP_KEY + ", " + FIELD_VALUE + ')';
        String addCall = getMapCall + '.' + addToMapCall;
        assertGeneratedCodeContains(addCall);
    }

    private void assertGeneratedCodeContains(CharSequence setterCall) {
        Generators.assertContains(jsOutput, setterCall);
    }

    private SingularFieldHandler singularHandler() {
        return (SingularFieldHandler) FieldHandlers.createFor(singularField(), jsOutput);
    }

    private RepeatedFieldHandler repeatedHandler() {
        return (RepeatedFieldHandler) FieldHandlers.createFor(repeatedField(), jsOutput);
    }

    private MapFieldHandler mapHandler() {
        return (MapFieldHandler) FieldHandlers.createFor(mapField(), jsOutput);
    }
}
