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

package io.spine.code.proto.ref;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.code.proto.FieldName;
import io.spine.value.StringTypeValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.code.proto.ref.BuiltIn.ALL;
import static io.spine.code.proto.ref.BuiltIn.EVENT_CONTEXT;
import static io.spine.code.proto.ref.BuiltIn.SELF;
import static io.spine.code.proto.ref.BuiltIn.checkTypeReference;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A reference to a field found in the {@code "by"} option value.
 */
public final class FieldRef extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    /**
     * Separates a type name from a field name.
     */
    private static final Splitter fieldNameSplit = Splitter.on('.');

    /**
     * Reference value parts separated by {@link #fieldNameSplit}.
     */
    private final ImmutableList<String> parts;

    /**
     * Reference to a proto type.
     */
    private final TypeRef typeRef;

    @VisibleForTesting
    FieldRef(String value) {
        super(checkValue(value));
        this.parts = parts(value);
        this.typeRef = TypeRef.parse(typeRef(value));
    }

    /**
     * Ensures that the passed value is not null, empty or blank, and if it contains a type
     * reference, it's not the {@linkplain BuiltIn#checkTypeReference(String)} suffix form}.
     */
    private static String checkValue(String value) {
        checkNotEmptyOrBlank(value);
        List<String> parts = parts(value);
        checkThat(!parts.isEmpty(), value);
        if (parts.size() >= 2) {
            // Contains the type part.
            checkTypeReference(parts.get(0));
        }
        parts.forEach(v -> checkThat(!v.trim()
                                       .isEmpty(), value));
        return value;
    }

    private static void checkThat(boolean b, String value) {
        checkArgument(b, "The value (`%s`) is not a valid field reference.", value);
    }

    private static ImmutableList<String> parts(String value) {
        List<String> elements = fieldNameSplit.splitToList(value);
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
     * Tells if the passed value reference all types.
     */
    public static boolean isWildcard(String typeReference) {
        checkTypeReference(typeReference);
        return ALL.value()
                  .equals(typeReference);
    }

    /**
     * Verifies if the reference is to a field in all types having a field with the referenced name.
     */
    public boolean isWildcard() {
        boolean result = typeRef.equals(ALL);
        return result;
    }

    /**
     * Verifies if the reference is to a field from the same type.
     */
    public boolean isInner() {
        boolean result = typeRef.equals(SELF);
        return result;
    }

    /**
     * Tells if the reference is for a message context field.
     */
    public boolean isContext() {
        boolean result = typeRef.equals(EVENT_CONTEXT);
        return result;
    }

    /**
     * Obtains the type name from the reference.
     *
     * @throws IllegalStateException if the reference does not have a type component
     * @see #hasType()
     */
    public String fullTypeName() {
        checkHasType();
        String value = value();
        return typeRef(value);
    }

    /**
     * Obtains type reference part from the field reference string.
     *
     * @return the string until the {@linkplain FieldName#TYPE_SEPARATOR field name separator},
     *         or en empty string, if type reference is not provided
     */
    private static String typeRef(String value) {
        int index = value.lastIndexOf(FieldName.TYPE_SEPARATOR);
        if (index == -1) {
            return "";
        }
        String result = value.substring(0, index)
                             .trim();
        return result;
    }

    private void checkHasType() {
        checkState(hasType(), "The field reference (`%s`) does not have the type.", value());
    }

    /**
     * Obtains a non-qualified name of the type from the reference.
     *
     * @throws IllegalStateException if the reference does not have a type component
     * @see #hasType()
     */
    public String simpleTypeName() {
        checkState(typeRef instanceof Direct,
                   "Unable to obtain a simple type name from the type reference `%s`.", typeRef);
        String result = ((Direct) typeRef).simpleTypeName();
        return result;
    }

    /**
     * Verifies if the reference contains a type name part.
     */
    public boolean hasType() {
        return parts.size() >= 2;
    }

    /**
     * Obtains the field name part of the reference.
     */
    public String fieldName() {
        return parts.get(parts.size() - 1);
    }

    /**
     * Verifies if the message type matches the one from the reference.
     *
     * <p>The method accepts all types if this instance is a wildcard type reference.
     */
    public boolean matchesType(Descriptor message) {
        checkNotNull(message);
        boolean result = typeRef.test(message);
        return result;
    }

    /**
     * Obtains the descriptor of the field with the name {@linkplain #fieldName()} referenced}
     * by this instance in the passed message.
     *
     * @param message the message in which to find the field
     * @return the descriptor of the field, or empty {@code Optional} if there is no a field with
     * the {@linkplain #fieldName()} referenced name}
     */
    public Optional<FieldDescriptor> find(Descriptor message) {
        checkNotNull(message);
        if (hasType() && !isWildcard()) {
            String referencedType = simpleTypeName();
            String messageType = message.getName();
            checkArgument(
                    matchesType(message),
                    "The referenced type (`%s`) does not match the type of the message (`%s`).",
                    referencedType,
                    messageType
            );
        }
        @Nullable FieldDescriptor result = message.findFieldByName(fieldName());
        return Optional.ofNullable(result);
    }
}
