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

package io.spine.json;

import com.google.common.collect.Lists;
import com.google.protobuf.Descriptors;
import com.google.protobuf.StringValue;
import com.google.protobuf.util.JsonFormat;
import io.spine.json.given.Node;
import io.spine.json.given.WrappedString;
import io.spine.test.Tests;
import io.spine.type.KnownTypes;
import io.spine.type.TypeUrl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static io.spine.base.Identifier.newUuid;
import static io.spine.json.Json.fromJson;
import static io.spine.json.Json.toCompactJson;
import static io.spine.json.Json.toJson;
import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Alexander Yevsyukov
 */
public class JsonShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void have_utility_ctor() {
        assertHasPrivateParameterlessCtor(Json.class);
    }

    @Test
    public void build_JsonFormat_registry_for_known_types() {
        JsonFormat.TypeRegistry typeRegistry = Json.typeRegistry();

        List<Descriptors.Descriptor> found = Lists.newLinkedList();
        for (TypeUrl typeUrl : KnownTypes.instance()
                                         .getAllUrls()) {
            Descriptors.Descriptor descriptor = typeRegistry.find(typeUrl.getTypeName());
            if (descriptor != null) {
                found.add(descriptor);
            }
        }

        assertFalse(found.isEmpty());
    }

    @Test
    public void toJson_fail_on_null() {
        thrown.expect(NullPointerException.class);
        toJson(Tests.nullRef());
    }

    @Test
    public void print_to_json() {
        StringValue value = toMessage("print_to_json");
        assertFalse(toJson(value).isEmpty());
    }

    @Test
    public void print_to_compact_json() {
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
    public void parse_from_json() {
        String idValue = newUuid();
        String jsonMessage = format("{value:%s}", idValue);
        WrappedString parsedValue = fromJson(jsonMessage, WrappedString.class);
        assertNotNull(parsedValue);
        assertEquals(idValue, parsedValue.getValue());
    }
}
