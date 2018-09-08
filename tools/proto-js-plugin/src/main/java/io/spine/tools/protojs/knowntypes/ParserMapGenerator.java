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
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.type.TypeUrl;

import java.util.Map.Entry;

import static io.spine.type.TypeUrl.of;

@SuppressWarnings("OverlyCoupledClass")
// Dependencies for listed known types.
public final class ParserMapGenerator {

    public static final String MAP_NAME = "parsers";

    private static final ImmutableMap<TypeUrl, String> parsers = parsers();

    private final JsGenerator jsGenerator;

    ParserMapGenerator(JsGenerator jsGenerator) {
        this.jsGenerator = jsGenerator;
    }

    public static boolean hasParser(TypeUrl typeUrl) {
        boolean hasParser = parsers.containsKey(typeUrl);
        return hasParser;
    }

    void generateJs() {
        jsGenerator.addEmptyLine();
        jsGenerator.exportMap(MAP_NAME);
        storeKnownTypeParsers();
        jsGenerator.quitMapDeclaration();
    }

    private void storeKnownTypeParsers() {
        ImmutableSet<Entry<TypeUrl, String>> entries = parsers.entrySet();
        for (UnmodifiableIterator<Entry<TypeUrl, String>> it = entries.iterator(); it.hasNext(); ) {
            Entry<TypeUrl, String> typeToParser = it.next();
            boolean isLastEntry = !it.hasNext();
            addMapEntry(typeToParser, isLastEntry);
        }
    }

    private void addMapEntry(Entry<TypeUrl, String> typeToParser, boolean isLastEntry) {
        String mapEntry = jsMapEntry(typeToParser);
        jsGenerator.addMapEntry(mapEntry, isLastEntry);
    }

    private static String jsMapEntry(Entry<TypeUrl, String> typeToParser) {
        TypeUrl typeUrl = typeToParser.getKey();
        String parserName = typeToParser.getValue();
        String newParserCall = "new " + parserName + "()";
        String mapEntry = "['" + typeUrl + "', " + newParserCall + ']';
        return mapEntry;
    }

    @SuppressWarnings("OverlyCoupledMethod") // Dependencies for listed known types.
    private static ImmutableMap<TypeUrl, String> parsers() {
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
