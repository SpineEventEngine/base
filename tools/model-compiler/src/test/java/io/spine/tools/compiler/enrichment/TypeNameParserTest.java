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

package io.spine.tools.compiler.enrichment;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.StringValue;
import io.spine.type.TypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static io.spine.option.OptionsProto.enrichment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TypeNameParser should")
class TypeNameParserTest {

    private static final String PACKAGE_PREFIX = "foo.bar.";
    private static final String MESSAGE_NAME = "AMessage";

    private final TypeNameParser parser = new TypeNameParser(enrichment, PACKAGE_PREFIX);

    @Test
    @DisplayName("add package prefix to unqualified type")
    void add_package_prefix_to_unqualified_type() {
        TypeName parsedTypes = parser.parseTypeName(MESSAGE_NAME);
        assertEquals(PACKAGE_PREFIX + MESSAGE_NAME, parsedTypes.value());
    }

    @Test
    @DisplayName("not add package prefix to fully qualified type")
    void not_add_package_prefix_to_fully_qualified_type() {
        String fqn = PACKAGE_PREFIX + MESSAGE_NAME;
        TypeName parsedType = parser.parseTypeName(fqn);
        assertEquals(fqn, parsedType.value());
    }

    @Test
    @DisplayName("return empty collection if option is not present")
    void return_empty_collection_if_option_is_not_present() {
        DescriptorProto definitionWithoutOption = StringValue.getDescriptor()
                                                             .toProto();
        Collection<TypeName> result = parser.parse(definitionWithoutOption);
        assertTrue(result.isEmpty());
    }
}
