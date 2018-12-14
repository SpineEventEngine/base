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

package io.spine.js.generate.parse.field;

import io.spine.code.js.FieldName;
import io.spine.js.generate.JsOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.js.generate.given.Generators.assertContains;
import static io.spine.js.generate.parse.FromJsonMethod.FROM_OBJECT;
import static io.spine.js.generate.parse.FromJsonMethod.FROM_OBJECT_ARG;
import static io.spine.js.generate.parse.field.FieldGenerator.FIELD_VALUE;
import static io.spine.js.generate.parse.field.MapFieldGenerator.ATTRIBUTE;
import static io.spine.js.generate.parse.field.MapFieldGenerator.MAP_KEY;
import static io.spine.js.generate.parse.field.RepeatedFieldGenerator.LIST_ITEM;
import static io.spine.js.generate.parse.field.given.Given.mapField;
import static io.spine.js.generate.parse.field.given.Given.repeatedField;
import static io.spine.js.generate.parse.field.given.Given.singularField;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("DuplicateStringLiteralInspection")
// Generated code duplication needed to check main class.
@DisplayName("FieldGenerator should")
class FieldGeneratorTest {

    private static final String JS_OBJECT = "jsObject";

    private JsOutput jsOutput;

    private SingularFieldGenerator singularGenerator;
    private RepeatedFieldGenerator repeatedGenerator;
    private MapFieldGenerator mapGenerator;

    @BeforeEach
    void setUp() {
        jsOutput = new JsOutput();
        singularGenerator = singularGenerator();
        repeatedGenerator = repeatedGenerator();
        mapGenerator = mapGenerator();
    }

    @Test
    @DisplayName("acquire field value by field JSON name")
    void acquireJsObject() {
        String fieldValue = singularGenerator.acquireFieldValue();
        String expected = FROM_OBJECT_ARG + '.' + singularField().getJsonName();
        assertEquals(expected, fieldValue);
    }

    @Nested
    @DisplayName("iterate")
    class Iterate {

        @Test
        @DisplayName("JS list items in case of repeated field")
        void repeated() {
            repeatedGenerator.iterateListValues(JS_OBJECT);
            String forEach = JS_OBJECT + ".forEach";
            assertContains(jsOutput, forEach);
            String forEachItems = '(' + LIST_ITEM + ", index, array)";
            assertContains(jsOutput, forEachItems);
        }

        @Test
        @DisplayName("JS object own properties in case of map field")
        void map() {
            String value = mapGenerator.iterateOwnAttributes(JS_OBJECT);
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
    void callPrecondition() {
        String fieldValue = singularGenerator.acquireFieldValue();
        singularGenerator.generate();
        String nullCheck = "if (" + fieldValue + " === null)";
        assertContains(jsOutput, nullCheck);
    }

    @Test
    @DisplayName("call field value parser to parse field value")
    void callParser() {
        String fieldValue = singularGenerator.acquireFieldValue();
        singularGenerator.generate();
        String typeName = singularField().getMessageType()
                                         .getFullName();
        String recursiveCall = typeName + '.' + FROM_OBJECT + '(' + fieldValue + ')';
        assertContains(jsOutput, recursiveCall);
    }

    @Test
    @DisplayName("parse object attribute value to obtain key in case of map field")
    void parseMapKey() {
        mapGenerator.generate();
        String parseAttribute = MAP_KEY + " = parseInt(" + ATTRIBUTE + ')';
        assertContains(jsOutput, parseAttribute);
    }

    @Test
    @DisplayName("set singular field")
    void setSingular() {
        singularGenerator.generate();
        FieldName fieldName = FieldName.from(singularField());
        String setterCall = "set" + fieldName + '(' + FIELD_VALUE + ')';
        assertContains(jsOutput, setterCall);
    }

    @Test
    @DisplayName("add value to repeated field")
    void addToRepeated() {
        repeatedGenerator.generate();
        FieldName fieldName = FieldName.from(repeatedField());
        String addCall = "add" + fieldName + '(' + FIELD_VALUE + ')';
        assertContains(jsOutput, addCall);
    }

    @Test
    @DisplayName("add value to map field")
    void addToMap() {
        mapGenerator.generate();
        FieldName fieldName = FieldName.from(mapField());
        String getMapCall = "get" + fieldName + "Map()";
        String addToMapCall = "set(" + MAP_KEY + ", " + FIELD_VALUE + ')';
        String addCall = getMapCall + '.' + addToMapCall;
        assertContains(jsOutput, addCall);
    }

    private SingularFieldGenerator singularGenerator() {
        return (SingularFieldGenerator) FieldGenerators.createFor(singularField(), jsOutput);
    }

    private RepeatedFieldGenerator repeatedGenerator() {
        return (RepeatedFieldGenerator) FieldGenerators.createFor(repeatedField(), jsOutput);
    }

    private MapFieldGenerator mapGenerator() {
        return (MapFieldGenerator) FieldGenerators.createFor(mapField(), jsOutput);
    }
}
