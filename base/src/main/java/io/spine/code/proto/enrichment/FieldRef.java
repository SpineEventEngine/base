/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.code.proto.enrichment;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.base.FieldPath;
import io.spine.base.FieldPaths;
import io.spine.code.proto.ref.TypeRef;
import io.spine.value.StringTypeValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.base.FieldPaths.findField;
import static io.spine.code.proto.enrichment.BuiltIn.ANY;
import static io.spine.code.proto.enrichment.BuiltIn.CONTEXT;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A reference to a field found in the {@code "by"} option value.
 */
public final class FieldRef extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    /**
     * A delimiter between a field name and its qualifier, which refers to another message
     * like {@code "context"}, or between nested field names like {@code "timestamp.seconds"}.
     */
    private static final String SEPARATOR = ".";

    /**
     * Separates tokens in a field reference.
     */
    private static final Splitter split = Splitter.on(SEPARATOR).trimResults();

    /**
     * Reference to a containing type.
     */
    private final TypeRef typeRef;

    /**
     * {@code true} if the referenced field is nested inside another field(s value.
     */
    private final FieldPath path;

    @VisibleForTesting
    FieldRef(String value) {
        super(checkValue(value));
        ImmutableList<String> parts = split(value);
        this.typeRef = CONTEXT.parse(parts.get(0))
                              .orElse(ANY);
        // If the first element is context reference, skip it from the path.
        ImmutableList<String> fieldPath =
                parts.subList((this.typeRef == CONTEXT ? 1 : 0), parts.size());
        this.path = FieldPaths.fromElements(fieldPath);
    }

    /**
     * Ensures that the passed value is not null, empty or blank.
     */
    private static String checkValue(String value) {
        checkNotEmptyOrBlank(value);
        checkArgument(
                !value.contains("*"),
                "Field reference cannot be wildcard. Found: `%s`.",
                value
        );
        List<String> parts = split(value);
        checkThat(!parts.isEmpty(), value);
        parts.forEach(v -> checkThat(!v.trim()
                                       .isEmpty(), value));
        return value;
    }

    private static void checkThat(boolean b, String value) {
        checkArgument(b, "The value (`%s`) is not a valid field reference.", value);
    }

    private static ImmutableList<String> split(String value) {
        List<String> elements = split.splitToList(value);
        return ImmutableList.copyOf(elements);
    }

    /**
     * Obtains references found in the passed field.
     */
    public static ImmutableList<FieldRef> allFrom(FieldDescriptorProto field) {
        ImmutableList<String> refs = ByOption.allFrom(field);
        ImmutableList.Builder<FieldRef> result = ImmutableList.builder();
        for (String ref : refs) {
            result.add(new FieldRef(ref));
        }
        return result.build();
    }

    /**
     * Verifies if the reference is to a field from the same type.
     */
    public boolean isInner() {
        boolean result = typeRef.equals(ANY);
        return result;
    }

    /**
     * Tells if the reference is for a message context field.
     */
    public boolean isContext() {
        boolean result = typeRef.equals(CONTEXT);
        return result;
    }

    /**
     * Obtains the field name part of the reference.
     *
     * <p>If the field reference is nested, the returned value is the name of the innermost field.
     */
    public String fieldName() {
        return path.getFieldName(path.getFieldNameCount() - 1);
    }

    /**
     * Verifies if the message type matches the one from the reference.
     *
     * <p>The method accepts all types if this instance is a wildcard type reference.
     */
    public boolean matchesType(Descriptor type) {
        checkNotNull(type);
        boolean typeMatches = typeRef.test(type);
        if (!typeMatches) {
            return false;
        }
        FieldDescriptor field = findField(path, type);
        return field != null;
    }

    /**
     * Obtains the descriptor of the field with the name {@linkplain #fieldName()} referenced}
     * by this instance in the passed message.
     *
     * @param type
     *         the type of the message in which to find the field
     * @return the descriptor of the field, or empty {@code Optional} if there is no a field with
     *         the {@linkplain #fieldName()} referenced name}
     */
    public Optional<FieldDescriptor> find(Descriptor type) {
        checkNotNull(type);
        @Nullable FieldDescriptor result = findField(path, type);
        return Optional.ofNullable(result);
    }
}
