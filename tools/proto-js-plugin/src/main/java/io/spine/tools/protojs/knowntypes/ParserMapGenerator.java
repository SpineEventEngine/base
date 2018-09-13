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
import io.spine.tools.protojs.code.JsOutput;
import io.spine.type.TypeUrl;

import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.type.TypeUrl.of;

/**
 * The generator which creates standard type parsers {@code Map}.
 *
 * <p>Parsers are stored in the map in the "{@linkplain TypeUrl type-url}-to-parser" format.
 *
 * <p>The parsers may be used to parse JSON via their {@code parse(value)} method.
 *
 * @apiNote
 * Like the other handlers and generators of this module, the {@code ParserMapGenerator} is meant
 * to operate on the common {@link JsOutput} passed on construction and thus its methods do not
 * return any generated code.
 *
 * @author Dmytro Kuzmin
 * @see KnownTypeParsersWriter
 */
@SuppressWarnings("OverlyCoupledClass")
// Dependencies for the listed Message types.
public final class ParserMapGenerator {

    /**
     * The exported map name.
     */
    public static final String MAP_NAME = "parsers";

    /**
     * The hard-coded map of known standard type parsers.
     *
     * <p>The map entry's value represents the JS type name of the parser.
     *
     * <p>Before adding the new entry to the map make sure the corresponding parser type is present
     * in the {@code known_type_parsers.js} resource.
     */
    private static final ImmutableMap<TypeUrl, String> parsers = parsers();

    private final JsOutput jsOutput;

    /**
     * Creates a new {@code ParserMapGenerator}.
     *
     * @param jsOutput
     *         the {@code JsOutput} which accumulates all the generated code
     */
    ParserMapGenerator(JsOutput jsOutput) {
        this.jsOutput = jsOutput;
    }

    /**
     * Checks if the JSON parser for the following {@code TypeUrl} is available.
     *
     * @param typeUrl
     *         the type URL to check
     * @return {@code true} if the parser for {@code TypeUrl} is present and {@code false}
     *         otherwise
     */
    public static boolean hasParser(TypeUrl typeUrl) {
        checkNotNull(typeUrl);
        boolean hasParser = parsers.containsKey(typeUrl);
        return hasParser;
    }

    /**
     * Stores parsers {@code Map} to the {@code jsOutput}.
     *
     * <p>The name of the exported map is the {@link #MAP_NAME}.
     */
    void generateJs() {
        jsOutput.addEmptyLine();
        jsOutput.exportMap(MAP_NAME);
        storeParsersToMap();
        jsOutput.quitMapDeclaration();
    }

    /**
     * Adds entries to the declared parsers {@code Map}.
     */
    private void storeParsersToMap() {
        ImmutableSet<Entry<TypeUrl, String>> entries = parsers.entrySet();
        for (UnmodifiableIterator<Entry<TypeUrl, String>> it = entries.iterator(); it.hasNext(); ) {
            Entry<TypeUrl, String> typeToParser = it.next();
            boolean isLastEntry = !it.hasNext();
            addMapEntry(typeToParser, isLastEntry);
        }
    }

    /**
     * Converts the {@linkplain #parsers parsers map} entry to the JS map entry and adds it to the
     * generated code.
     */
    private void addMapEntry(Entry<TypeUrl, String> typeToParser, boolean isLastEntry) {
        String mapEntry = jsMapEntry(typeToParser);
        jsOutput.addMapEntry(mapEntry, isLastEntry);
    }

    /**
     * Converts the {@linkplain Entry Java Map Entry} from the {@link #parsers} to the JS
     * {@code Map} entry.
     */
    private static String jsMapEntry(Entry<TypeUrl, String> typeToParser) {
        TypeUrl typeUrl = typeToParser.getKey();
        String parserName = typeToParser.getValue();
        String newParserCall = "new " + parserName + "()";
        String mapEntry = "['" + typeUrl + "', " + newParserCall + ']';
        return mapEntry;
    }

    @SuppressWarnings("OverlyCoupledMethod") // Dependencies for the listed Message types.
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
