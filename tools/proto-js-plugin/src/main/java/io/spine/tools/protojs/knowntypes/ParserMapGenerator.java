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

package io.spine.tools.protojs.knowntypes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import com.google.protobuf.Any;
import com.google.protobuf.BytesValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Duration;
import com.google.protobuf.Empty;
import com.google.protobuf.FieldMask;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.ListValue;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.Value;
import io.spine.tools.protojs.code.JsWriter;
import io.spine.type.TypeUrl;

import java.util.Map.Entry;

import static io.spine.type.TypeUrl.of;

@SuppressWarnings("OverlyCoupledClass")
        // Dependencies for listed known types.
public class ParserMapGenerator {

    public static final ImmutableMap<TypeUrl, String> JS_PARSER_NAMES = jsParserNames();

    // todo make it so we don't need to call map name from file import and just can call "get"
    public static final String MAP_NAME = "parsers";

    private final JsWriter jsWriter;

    ParserMapGenerator(JsWriter jsWriter) {
        this.jsWriter = jsWriter;
    }

    void generateParserMap() {
        jsWriter.addLine("export const " + MAP_NAME + " = new Map([");
        jsWriter.increaseDepth();
        ImmutableSet<Entry<TypeUrl, String>> entries = JS_PARSER_NAMES.entrySet();
        for (UnmodifiableIterator<Entry<TypeUrl, String>> it = entries.iterator(); it.hasNext(); ) {
            Entry<TypeUrl, String> entry = it.next();
            boolean hasNext = it.hasNext();
            addMapEntry(entry, hasNext);
        }
        jsWriter.decreaseDepth();
        jsWriter.addLine("]);");
    }

    private void addMapEntry(Entry<TypeUrl, String> entry, boolean hasNext) {
        TypeUrl typeUrl = entry.getKey();
        String parserName = entry.getValue();
        String newParserInstance = "new " + parserName + "()";
        String mapEntry = mapEntry(typeUrl, newParserInstance);
        StringBuilder mapEntryBuilder = new StringBuilder(mapEntry);
        if (hasNext) {
            mapEntryBuilder.append(',');
        }
        String line = mapEntryBuilder.toString();
        jsWriter.addLine(line);
    }

    private static String mapEntry(TypeUrl typeUrl, String newParserInstance) {
        String mapEntry = "['" + typeUrl + "', " + newParserInstance + ']';
        return mapEntry;
    }

    @SuppressWarnings("OverlyCoupledMethod") // Dependencies for listed known types.
    private static ImmutableMap<TypeUrl, String> jsParserNames() {
        ImmutableMap<TypeUrl, String> jsParserNames = ImmutableMap
                .<TypeUrl, String>builder()
                .put(of(BytesValue.class), "BytesValueParser")
                .put(of(DoubleValue.class), "DoubleValueParser")
                .put(of(FloatValue.class), "FloatValueParser")
                .put(of(Int32Value.class), "Int32ValueParser")
                .put(of(Int64Value.class), "Int64ValueParser")
                .put(of(StringValue.class), "StringValueParser")
                .put(of(UInt32Value.class), "UInt32ValueParser")
                .put(of(UInt64Value.class), "UInt64ValueParser")
                .put(of(Value.class), "ValueParser")
                .put(of(ListValue.class), "ListValueParser")
                .put(of(Empty.class), "EmptyParser")
                .put(of(Timestamp.class), "TimestampParser")
                .put(of(Duration.class), "DurationParser")
                .put(of(FieldMask.class), "FieldMaskParser")
                .put(of(Any.class), "AnyParser")
                .build();
        return jsParserNames;
    }
}
