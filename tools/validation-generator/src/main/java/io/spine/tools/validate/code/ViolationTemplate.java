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

package io.spine.tools.validate.code;

import com.squareup.javapoet.CodeBlock;
import io.spine.base.FieldPath;
import io.spine.code.proto.FieldDeclaration;
import io.spine.protobuf.TypeConverter;
import io.spine.type.MessageType;
import io.spine.validate.ConstraintViolation;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * An {@link Expression} which creates a new {@link ConstraintViolation}.
 */
public final class ViolationTemplate implements Expression<ConstraintViolation> {

    private final String message;
    private final @Nullable Expression fieldValue;
    private final MessageType type;
    private final FieldPath field;
    private final @Nullable Expression<Iterable<ConstraintViolation>> nestedViolations;

    private ViolationTemplate(Builder builder) {
        this.message = builder.message;
        this.fieldValue = builder.fieldValue;
        this.type = builder.type;
        this.field = builder.field;
        this.nestedViolations = builder.nestedViolations;
    }

    @Override
    public CodeBlock toCode() {
        CodeBlock.Builder builder = CodeBlock
                .builder()
                .add("$T.newBuilder()", ConstraintViolation.class)
                .add(".setMsgFormat($S)", message)
                .add(".setTypeName($S)", type.name().value())
                .add(".setFieldPath($T.newBuilder()", FieldPath.class);
        for (String fieldName : field.getFieldNameList()) {
            builder.add(".addFieldName($S)", fieldName);
        }
        builder.add(".build())");
        if (fieldValue != null) {
            builder.add(".setFieldValue($T.toAny($L))", TypeConverter.class, fieldValue.toCode());
        }
        if (nestedViolations != null) {
            builder.add(".addAllViolation($L)", nestedViolations.toCode());
        }
        builder.add(".build()");
        return builder.build();
    }

    @Override
    public String toString() {
        return toCode().toString();
    }

    /**
     * Creates a new instance of {@code Builder} for {@code ViolationTemplate} instances.
     *
     * @return new instance of {@code Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder forField(FieldDeclaration field) {
        checkNotNull(field);
        return newBuilder()
                .setType(field.declaringType())
                .setField(field.name().value());
    }

    /**
     * A builder for the {@code ViolationTemplate} instances.
     */
    public static final class Builder {

        private String message;
        private @Nullable Expression fieldValue;
        private MessageType type;
        private FieldPath field;
        private @Nullable Expression<Iterable<ConstraintViolation>> nestedViolations;

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        public Builder setMessage(String message) {
            this.message = checkNotNull(message);
            return this;
        }

        public Builder setFieldValue(Expression fieldValue) {
            this.fieldValue = checkNotNull(fieldValue);
            return this;
        }

        public Builder setType(MessageType type) {
            this.type = checkNotNull(type);
            return this;
        }

        public Builder setField(String... fieldNames) {
            this.field = FieldPath
                    .newBuilder()
                    .addAllFieldName(asList(fieldNames))
                    .build();
            return this;
        }

        public Builder
        setNestedViolations(Expression<Iterable<ConstraintViolation>> nestedViolations) {
            this.nestedViolations = checkNotNull(nestedViolations);
            return this;
        }

        /**
         * Creates a new instance of {@code ViolationTemplate}.
         *
         * @return new instance of {@code ViolationTemplate}
         */
        public ViolationTemplate build() {
            checkNotNull(message);
            checkNotNull(type);
            checkNotNull(field);
            return new ViolationTemplate(this);
        }
    }
}
