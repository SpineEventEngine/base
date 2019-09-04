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

package io.spine.base;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import io.spine.type.TypeName;
import io.spine.value.ValueHolder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.TextFormat.shortDebugString;
import static io.spine.base.FieldPaths.checkName;
import static io.spine.base.FieldPaths.checkNotEmpty;
import static io.spine.base.FieldPaths.classOf;
import static io.spine.base.FieldPaths.fieldIn;
import static io.spine.base.FieldPaths.join;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A reference to a Protobuf message field.
 *
 * @apiNote This class aggregates {@link FieldPath} for augmenting this generated class with
 * useful methods (instead of using static utilities). This approach is used (instead of using
 * {@link io.spine.annotation.GeneratedMixin GeneratedMixin}) because Model Compiler itself depends
 * on this package. Thus, {@code GeneratedMixin} cannot be used for augmenting generated classes
 * that belong to it.
 */
@Immutable
public final class Field extends ValueHolder<FieldPath> {

    private static final long serialVersionUID = 0L;

    private Field(FieldPath path) {
        super(path);
    }

    private static Field create(FieldPath path) {
        checkNotEmpty(path);
        return new Field(path);
    }

    /**
     * Creates a new field reference by the passed path.
     */
    public static Field withPath(FieldPath path) {
        checkNotNull(path);
        return create(path);
    }

    /**
     * Parses the passed field path.
     *
     * @param path
     *         non-empty field path
     * @return the field reference parsed from the path
     */
    public static Field parse(String path) {
        checkNotNull(path);
        FieldPath fp = FieldPaths.doParse(path);
        return create(fp);
    }

    /**
     * Creates a new field reference by its name.
     *
     * <p>The passed string is the direct reference to the field, not a field path.
     * Therefore it must not contain the dot separator.
     */
    public static Field named(String fieldName) {
        checkName(fieldName);
        FieldPath path = FieldPaths.create(fieldName);
        Field result = create(path);
        return result;
    }

    /** Obtains the path of the field. */
    public FieldPath path() {
        return value();
    }

    /**
     * Obtains the value of the field in the passed message.
     *
     * @param holder
     *         the message which potentially has the referenced field
     * @return the value of the field or empty {@code Optional} if the field is not defined
     *         in this type of messages
     */
    public Optional<Object> findValue(Message holder) {
        Object value = FieldPaths.doGetValue(path(), holder, false);
        return Optional.ofNullable(value);
    }

    /**
     * Obtains the value of the field (which must exist) in the passed message.
     *
     * @throws IllegalStateException if the type of the passed message does not declare this field
     */
    public Object valueIn(Message holder) {
        Object result = findValue(holder).orElseThrow(
                () -> newIllegalStateException("Unable to get the field `%s` from `%s`.",
                                               this, shortDebugString(holder))
        );
        return result;
    }
    /**
     * Obtains a descriptor of the referenced field in the passed message type.
     *
     * @return the descriptor, if there is such a field in the passed type, or empty
     *  {@code Optional} if the field is not declared
     */
    public Optional<FieldDescriptor> findDescriptor(Descriptor message) {
        @Nullable FieldDescriptor field = fieldIn(path(), message);
        return Optional.ofNullable(field);
    }

    /**
     * Obtains the type of the referenced field in the passed message class.
     */
    public Optional<Class<?>> findType(Class<? extends Message> holderType) {
        Descriptor message = TypeName.of(holderType).messageDescriptor();
        @Nullable FieldDescriptor field = fieldIn(path(), message);
        if (field == null) {
            return Optional.empty();
        }
        Class<?> result = classOf(field);
        return Optional.of(result);
    }

    /**
     * Obtains a string value of the field path.
     *
     * <p>Unlike {@link Message#toString()}, which produces diagnostics output, this method
     * returns the string form of {@code some_field.nested_field.nested_deeper}.
     */
    @Override
    public String toString() {
        return join(value().getFieldNameList());
    }
}
