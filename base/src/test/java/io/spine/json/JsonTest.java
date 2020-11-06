/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.json;

import com.google.common.collect.Lists;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.StringValue;
import com.google.protobuf.TypeRegistry;
import io.spine.json.given.Node;
import io.spine.json.given.WrappedString;
import io.spine.testing.UtilityClassTest;
import io.spine.type.KnownTypes;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.base.Identifier.newUuid;
import static io.spine.json.Json.fromJson;
import static io.spine.json.Json.toCompactJson;
import static io.spine.json.Json.toJson;
import static io.spine.testing.Assertions.assertNpe;
import static io.spine.testing.Tests.nullRef;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Json utility class should")
class JsonTest extends UtilityClassTest<Json> {

    JsonTest() {
        super(Json.class);
    }

    @Test
    @DisplayName("build JsonFormat registry for known types")
    void knownTypes() {
        TypeRegistry typeRegistry = Json.typeRegistry();

        List<Descriptor> found = Lists.newLinkedList();
        for (TypeUrl typeUrl : KnownTypes.instance()
                                         .allUrls()) {
            Descriptor descriptor = typeRegistry.find(typeUrl.toTypeName()
                                                             .value());
            if (descriptor != null) {
                found.add(descriptor);
            }
        }

        assertFalse(found.isEmpty());
    }

    @Test
    @DisplayName("not allow null message")
    void rejectNulls() {
        assertNpe(() -> toJson(nullRef()));
    }

    @Test
    @DisplayName("print to JSON")
    void print() {
        StringValue value = StringValue.of("print_to_json");
        assertFalse(toJson(value).isEmpty());
    }

    @Test
    @DisplayName("print to compact JSON")
    void printCompact() {
        String idValue = newUuid();
        Node node = Node.newBuilder()
                        .setName(idValue)
                        .setRight(Node.getDefaultInstance())
                        .build();
        String result = toCompactJson(node);
        assertFalse(result.isEmpty());
        assertFalse(result.contains(System.lineSeparator()));
    }

    @Test
    @DisplayName("parse from JSON")
    void parse() {
        String idValue = newUuid();
        String jsonMessage = format("{\"value\": \"%s\"}", idValue);
        WrappedString parsedValue = fromJson(jsonMessage, WrappedString.class);
        assertNotNull(parsedValue);
        assertEquals(idValue, parsedValue.getValue());
    }

    @Test
    @DisplayName("parse from JSON with unknown values")
    void parseUnknown() {
        String idValue = newUuid();
        String jsonMessage = format("{\"value\": \"%s\", \"newField\": \"newValue\"}", idValue);
        WrappedString parsedValue = fromJson(jsonMessage, WrappedString.class);
        assertNotNull(parsedValue);
        assertEquals(idValue, parsedValue.getValue());
    }
}
