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

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import io.spine.base.FieldPath;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.protobuf.TypeConverter;
import io.spine.tools.validate.FieldAccess;
import io.spine.type.MessageType;
import io.spine.validate.ConstraintViolation;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * An {@link Expression} which creates a new {@link ConstraintViolation}.
 */
public final class NewViolation implements Expression<ConstraintViolation> {

    private final String message;
    private final ImmutableList<String> params;
    private final @Nullable FieldAccess fieldValue;
    private final MessageType type;
    private final FieldPath field;
    private final @Nullable Expression<? extends Iterable<ConstraintViolation>> nestedViolations;

    private NewViolation(Builder builder) {
        this.message = builder.message;
        this.fieldValue = builder.fieldValue;
        this.type = builder.type;
        this.field = builder.field;
        this.nestedViolations = builder.nestedViolations;
        this.params = ImmutableList.copyOf(builder.params);
    }

    @Override
    public CodeBlock toCode() {
        @SuppressWarnings("DuplicateStringLiteralInspection")
        CodeBlock.Builder builder = CodeBlock
                .builder()
                .add("$T.newBuilder()", ConstraintViolation.class)
                .add(".setMsgFormat($S)", message)
                .add(".setTypeName($S)", type.name().value());
        addFieldValue(builder);
        addViolations(builder);
        addFieldPath(builder);
        addParams(builder);
        builder.add(".build()");
        return builder.build();
    }

    private void addViolations(CodeBlock.Builder builder) {
        if (nestedViolations != null) {
            builder.add(".addAllViolation($L)", nestedViolations.toCode());
        }
    }

    private void addFieldValue(CodeBlock.Builder builder) {
        if (fieldValue != null) {
            builder.add(".setFieldValue($T.toAny($L))", TypeConverter.class, fieldValue.toCode());
        }
    }

    private void addFieldPath(CodeBlock.Builder builder) {
        builder.add(".setFieldPath($T.newBuilder()", FieldPath.class);
        for (String fieldName : field.getFieldNameList()) {
            builder.add(".addFieldName($S)", fieldName);
        }
        builder.add(".build())");
    }

    private void addParams(CodeBlock.Builder builder) {
        for (String param : params) {
            builder.add(".addParam($S)", param);
        }
    }

    @Override
    public String toString() {
        return toCode().toString();
    }

    /**
     * Creates a new instance of {@code Builder} for {@code NewViolation} instances.
     *
     * <p>The builder is preset with the declaring type and name of the given field.
     *
     * @return new instance of {@code Builder}
     */
    public static Builder forField(FieldContext field) {
        checkNotNull(field);
        FieldDeclaration declaration = field.targetDeclaration();
        return new Builder()
                .setType(declaration.declaringType())
                .setField(field.fieldPath().getFieldNameList());
    }

    public static Builder forMessage(FieldContext context, MessageType type) {
        checkNotNull(context);
        return new Builder()
                .setType(type)
                .setField(context.fieldPath().getFieldNameList());
    }

    /**
     * A builder for the {@code ViolationTemplate} instances.
     */
    public static final class Builder {

        private String message;
        private @Nullable FieldAccess fieldValue;
        private MessageType type;
        private FieldPath field;
        private @Nullable Expression<? extends Iterable<ConstraintViolation>> nestedViolations;
        private final List<String> params = new ArrayList<>();

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        public Builder setMessage(String message) {
            this.message = checkNotNull(message);
            return this;
        }

        public Builder setFieldValue(FieldAccess fieldValue) {
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

        public Builder setField(List<String> fieldNames) {
            this.field = FieldPath
                    .newBuilder()
                    .addAllFieldName(fieldNames)
                    .build();
            return this;
        }

        public Builder
        setNestedViolations(Expression<? extends Iterable<ConstraintViolation>> nestedViolations) {
            this.nestedViolations = checkNotNull(nestedViolations);
            return this;
        }

        public Builder addParam(String... value) {
            params.addAll(asList(value));
            return this;
        }

        /**
         * Creates a new instance of {@code ViolationTemplate}.
         *
         * @return new instance of {@code ViolationTemplate}
         */
        public NewViolation build() {
            checkNotNull(message);
            checkNotNull(type);
            checkNotNull(field);
            return new NewViolation(this);
        }
    }
}
