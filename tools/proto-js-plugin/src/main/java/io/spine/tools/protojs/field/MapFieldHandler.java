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

import com.google.common.annotations.VisibleForTesting;
import io.spine.tools.protojs.field.parser.FieldValueParser;

import static io.spine.tools.protojs.field.Fields.capitalizedName;
import static io.spine.tools.protojs.message.MessageHandler.MESSAGE;

public final class MapFieldHandler extends AbstractFieldHandler {

    @VisibleForTesting
    static final String ATTRIBUTE = "attribute";

    @VisibleForTesting
    static final String MAP_KEY = "mapKey";

    // todo mention in doc somewhere that key and value of the matrix are also fields of 'Entry'
    // message type, so they are processed by FieldValueParser too for this reason.
    private final FieldValueParser keyParser;

    private MapFieldHandler(Builder builder) {
        super(builder);
        this.keyParser = builder.keyParser;
    }

    @Override
    public void generateJs() {
        String jsObject = acquireJsObject();
        String value = iterateOwnAttributes(jsObject);
        parseMapKey();
        setFieldValue(value);
        exitOwnAttributeIteration();
    }

    @Override
    String setterFormat() {
        String fieldName = capitalizedName(field());
        String getMap = "get" + fieldName + "Map()";
        String setMapValue = "set(" + MAP_KEY + ", %s)";
        String addToMapFormat = MESSAGE + '.' + getMap + '.' + setMapValue + ';';
        return addToMapFormat;
    }

    @VisibleForTesting
    void parseMapKey() {
        keyParser.parseIntoVariable(ATTRIBUTE, MAP_KEY);
    }

    @VisibleForTesting
    String iterateOwnAttributes(String jsObject) {
        jsGenerator().ifNotNullOrUndefined(jsObject);
        jsGenerator().enterBlock("for (let " + ATTRIBUTE + " in " + jsObject + ')');
        jsGenerator().enterIfBlock(jsObject + ".hasOwnProperty(" + ATTRIBUTE + ')');
        String value = jsObject + '[' + ATTRIBUTE + ']';
        return value;
    }

    @VisibleForTesting
    void exitOwnAttributeIteration() {
        jsGenerator().exitBlock();
        jsGenerator().exitBlock();
        jsGenerator().exitBlock();
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static class Builder extends AbstractFieldHandler.Builder<Builder> {

        private FieldValueParser keyParser;

        Builder setKeyParser(FieldValueParser keyParser) {
            this.keyParser = keyParser;
            return self();
        }

        @Override
        Builder self() {
            return this;
        }

        @Override
        MapFieldHandler build() {
            return new MapFieldHandler(this);
        }
    }
}
