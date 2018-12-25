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

package io.spine.code.java;

import com.google.common.collect.ImmutableSet;
import io.spine.code.proto.FieldDeclaration;

import java.io.Serializable;
import java.util.Collection;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static io.spine.code.java.AccessorTemplate.prefixed;
import static io.spine.code.java.AccessorTemplate.prefixedAndSuffixed;

public final class GeneratedAccessors implements Serializable {

    private static final long serialVersionUID = 0L;

    private static final AccessorTemplate GET_TEMPLATE = prefixed("get");
    private static final AccessorTemplate GET_COUNT_TEMPLATE = prefixedAndSuffixed("get", "Count");
    private static final AccessorTemplate SET_TEMPLATE = prefixed("set");
    @SuppressWarnings("DuplicateStringLiteralInspection") // Same prefixes used in `VBuilder`s.
    private static final AccessorTemplate CLEAR_TEMPLATE = prefixed("clear");
    private static final String BYTES = "Bytes";

    private final FieldName propertyName;
    private final Type type;

    private GeneratedAccessors(FieldName propertyName, Type type) {
        this.propertyName = propertyName;
        this.type = type;
    }

    public static GeneratedAccessors forField(FieldDeclaration field) {
        FieldName javaFieldName = FieldName.from(field.name());
        Type type = Type.of(field);
        return new GeneratedAccessors(javaFieldName, type);
    }

    public ImmutableSet<String> names() {
        ImmutableSet<String> names = names(type.templates);
        return names;
    }

    private ImmutableSet<String> names(Collection<AccessorTemplate> templates) {
        return templates.stream()
                        .map(template -> template.format(propertyName))
                        .collect(toImmutableSet());
    }

    private enum Type {

        SINGULAR(ImmutableSet.of(prefixed("has"),
                                 GET_TEMPLATE,
                                 prefixedAndSuffixed("get", BYTES),
                                 SET_TEMPLATE,
                                 prefixedAndSuffixed("set", BYTES),
                                 CLEAR_TEMPLATE)),

        REPEATED(ImmutableSet.of(GET_TEMPLATE,
                                 prefixedAndSuffixed("get", "List"),
                                 SET_TEMPLATE,
                                 prefixed("add"),
                                 prefixed("allAll"),
                                 CLEAR_TEMPLATE)),

        @SuppressWarnings("DuplicateStringLiteralInspection") // Same prefixes used in `VBuilder`s.
        MAP(ImmutableSet.of(GET_TEMPLATE,
                            GET_COUNT_TEMPLATE,
                            prefixedAndSuffixed("get", "Map"),
                            prefixedAndSuffixed("get", "OrDefault"),
                            prefixedAndSuffixed("get", "OrThrow"),
                            prefixed("contains"),
                            CLEAR_TEMPLATE,
                            prefixed("put"),
                            prefixed("remove"),
                            prefixed("putAll")));

        private final ImmutableSet<AccessorTemplate> templates;

        Type(ImmutableSet<AccessorTemplate> templates) {
            this.templates = templates;
        }

        private static Type of(FieldDeclaration declaration) {
            if (declaration.isMap()) {
                return MAP;
            } else if (declaration.isRepeated()) {
                return REPEATED;
            } else {
                return SINGULAR;
            }
        }
    }
}
