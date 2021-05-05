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

package io.spine.js.generate.field;

import com.google.common.annotations.VisibleForTesting;
import io.spine.tools.js.code.FieldName;
import io.spine.js.generate.field.parser.FieldParser;

/**
 * The generator for the {@code map} Protobuf fields.
 *
 * <p>The generator expects a plain JS object as an input, treating its properties as the Protobuf
 * map entries.
 *
 * @implNote
 * This class holds the additional {@link #keyParser} which parses the map key from the JS object
 * attribute.
 *
 * <p>This is necessary as all proto {@code map} keys are converted to {@code string}s in JSON and
 * thus the object properties will also be of {@code string} type.
 *
 * <p>The {@link #precondition} and {@link #parser} from the superclass are used to process the
 * {@code map} value before adding it to the field.
 */
final class MapFieldGenerator extends FieldGenerator {

    /**
     * The variable holding the JS object own attribute during the iteration.
     */
    @VisibleForTesting
    static final String ATTRIBUTE = "attribute";

    /**
     * The variable which contains the parsed {@code map} key by which we can add the value to the
     * field.
     */
    @VisibleForTesting
    static final String MAP_KEY = "mapKey";

    private final FieldParser keyParser;

    private MapFieldGenerator(Builder builder) {
        super(builder);
        this.keyParser = builder.keyParser;
    }

    @Override
    public void generate() {
        String fieldValue = acquireFieldValue();
        String value = iterateOwnAttributes(fieldValue);
        parseMapKey();
        mergeFieldValue(value);
        exitOwnAttributeIteration();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The merge format for the {@code map} field is getting the field through getter and then
     * using the standard JS {@code Map.set} function to set the value.
     */
    @Override
    String mergeFormat() {
        FieldName fieldName = FieldName.from(field());
        String getMap = "get" + fieldName + "Map()";
        String setMapValue = "set(" + MAP_KEY + ", %s)";
        String addToMapFormat = targetVariable() + '.' + getMap + '.' + setMapValue + ';';
        return addToMapFormat;
    }

    /**
     * Generates the code to iterate own properties of the given JS object.
     *
     * <p>Checks the JS object to be not {@code null} or {@code undefined}.
     *
     * @param jsObject
     *         the name of the variable holding the JS object to iterate
     * @return the expression which represents the object property value
     */
    @VisibleForTesting
    String iterateOwnAttributes(String jsObject) {
        jsOutput().ifNotNullOrUndefined(jsObject);
        jsOutput().enterBlock("for (let " + ATTRIBUTE + " in " + jsObject + ')');
        jsOutput().enterIfBlock(jsObject + ".hasOwnProperty(" + ATTRIBUTE + ')');
        String value = jsObject + '[' + ATTRIBUTE + ']';
        return value;
    }

    /**
     * Generates the code to exit the own properties iteration.
     *
     * <p>Returns the cursor to the {@code fromObject} method level.
     */
    private void exitOwnAttributeIteration() {
        jsOutput().exitBlock();
        jsOutput().exitBlock();
        jsOutput().exitBlock();
    }

    /**
     * Generates the code which parses the proto {@linkplain #MAP_KEY map key} from the object
     * {@linkplain #ATTRIBUTE attribute name}, which is always a {@code string}.
     */
    private void parseMapKey() {
        keyParser.parseIntoVariable(ATTRIBUTE, MAP_KEY);
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static class Builder extends FieldGenerator.Builder<Builder> {

        private FieldParser keyParser;

        Builder setKeyParser(FieldParser keyParser) {
            this.keyParser = keyParser;
            return self();
        }

        @Override
        Builder self() {
            return this;
        }

        @Override
        MapFieldGenerator build() {
            return new MapFieldGenerator(this);
        }
    }
}
