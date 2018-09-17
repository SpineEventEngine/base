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

package io.spine.tools.protojs.field;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.protojs.field.parser.FieldParser;
import io.spine.tools.protojs.field.precondition.FieldPrecondition;
import io.spine.tools.protojs.generate.JsCodeGenerator;
import io.spine.tools.protojs.generate.JsOutput;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.message.MessageGenerator.FROM_OBJECT_ARG;
import static java.lang.String.format;

/**
 * The common base for the {@link FieldHandler} implementations.
 *
 * <p>The class generates the JS code common for all kinds of field handlers including calling the
 * {@linkplain FieldPrecondition field precondition} and the {@linkplain FieldParser field parser}.
 *
 * @author Dmytro Kuzmin
 */
public abstract class FieldGenerator extends JsCodeGenerator {

    /**
     * The variable holding the value parsed by the {@link FieldParser} and then used to set the
     * field.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
    @VisibleForTesting
    static final String FIELD_VALUE = "value";

    private final FieldDescriptor field;
    private final FieldPrecondition checker;
    private final FieldParser parser;

    FieldGenerator(Builder builder) {
        super(builder.jsOutput);
        this.field = builder.field;
        this.checker = builder.checker;
        this.parser = builder.parser;
    }

    /**
     * Returns the property of the {@code fromObject} method argument which corresponds to the
     * processed field.
     *
     * @return the field value as parsed from the JSON
     */
    String acquireFieldValue() {
        String fieldJsonName = field.getJsonName();
        String jsObject = FROM_OBJECT_ARG + '.' + fieldJsonName;
        return jsObject;
    }

    /**
     * Generates the code necessary to merge the field value with the specified JS value.
     *
     * <p>"Merge" implies either setting the field value in case of singular field or adding the
     * value to the {@code repeated}/{@code map} field.
     *
     * <p>The class descendants have to specify the desired action via overriding the
     * {@link #mergeFormat()} method.
     *
     * @param value
     *         the name of the variable containing the value to set the field to
     */
    void mergeFieldValue(String value) {
        checker.performNullCheck(value, mergeFormat());
        parser.parseIntoVariable(value, FIELD_VALUE);
        merge(FIELD_VALUE);
        checker.exitNullCheck();
    }

    FieldDescriptor field() {
        return field;
    }

    @VisibleForTesting
    FieldPrecondition checker() {
        return checker;
    }

    @VisibleForTesting
    FieldParser parser() {
        return parser;
    }

    /**
     * Generates the code which calls the field merge action on the specified value.
     *
     * <p>The value is assumed to be already {@linkplain FieldPrecondition checked} and
     * {@linkplain FieldParser parsed}.
     *
     * @param value
     *         the name of the variable with the value to set
     * @see #mergeFormat()
     */
    private void merge(String value) {
        String mergeFormat = mergeFormat();
        String setValue = format(mergeFormat, value);
        jsOutput().addLine(setValue);
    }

    /**
     * Returns the format of the set/add action which can be used to merge the field value from
     * the variable.
     *
     * <p>The format should have exactly one placeholder - {@code %s} - where the variable name
     * will be inserted.
     *
     * @return the field merger format
     */
    abstract String mergeFormat();

    /**
     * The generic builder for the classes-descendants.
     *
     * @param <B>
     *         the class of the Builder itself
     */
    abstract static class Builder<B extends Builder<B>> {

        private FieldDescriptor field;
        private FieldPrecondition checker;
        private FieldParser parser;
        private JsOutput jsOutput;

        B setField(FieldDescriptor field) {
            this.field = checkNotNull(field);
            return self();
        }

        B setChecker(FieldPrecondition checker) {
            this.checker = checkNotNull(checker);
            return self();
        }

        B setParser(FieldParser parser) {
            this.parser = checkNotNull(parser);
            return self();
        }

        B setJsOutput(JsOutput jsOutput) {
            this.jsOutput = checkNotNull(jsOutput);
            return self();
        }

        /**
         * <strong>Must</strong> return {@code this} in classes-descendants.
         */
        abstract B self();

        abstract FieldGenerator build();
    }
}
