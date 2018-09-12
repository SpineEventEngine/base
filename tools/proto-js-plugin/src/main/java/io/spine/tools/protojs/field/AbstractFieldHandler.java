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
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.field.checker.FieldValueChecker;
import io.spine.tools.protojs.field.parser.FieldValueParser;

import static io.spine.tools.protojs.message.MessageHandler.FROM_OBJECT_ARG;
import static java.lang.String.format;

/**
 * The base class for the {@link FieldHandler} implementations.
 *
 * <p>The class generates the JS code common for all kinds of field handlers including calling the
 * {@linkplain FieldValueChecker field value checker} and the
 * {@linkplain FieldValueParser field value parser} to check and parse the field value respectively.
 *
 * <p>All the generated code is stored in the {@link JsGenerator} passed on construction.
 *
 * @author Dmytro Kuzmin
 */
abstract class AbstractFieldHandler implements FieldHandler {

    /**
     * The name of the value parsed by the {@link FieldValueParser} and then used to set the field.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
    @VisibleForTesting
    static final String FIELD_VALUE = "value";

    private final FieldDescriptor field;
    private final FieldValueChecker checker;
    private final FieldValueParser parser;
    private final JsGenerator jsGenerator;

    AbstractFieldHandler(Builder builder) {
        this.field = builder.field;
        this.checker = builder.checker;
        this.parser = builder.parser;
        this.jsGenerator = builder.jsGenerator;
    }

    /**
     * Returns the corresponding field value of the {@code fromObject} method argument.
     *
     * @return the field value in the form of JS object
     */
    String acquireJsObject() {
        String fieldJsonName = field.getJsonName();
        String jsObject = FROM_OBJECT_ARG + '.' + fieldJsonName;
        return jsObject;
    }

    /**
     * Generates the JS code necessary to merge the field value with the specified JS value.
     *
     * <p>"Merge" implies either setting the field value in case of singular field or adding the
     * value to the {@code repeated}/{@code map} field.
     *
     * <p>The class descendants have to specify the desired action via overriding the
     * {@link #mergeFormat()} method.
     *
     * @param value
     *         the name of the variable containing JS value to set the field to
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

    JsGenerator jsGenerator() {
        return jsGenerator;
    }

    @VisibleForTesting
    FieldValueChecker checker() {
        return checker;
    }

    @VisibleForTesting
    FieldValueParser parser() {
        return parser;
    }

    /**
     * Generates the JS code which calls the field merge action on the specified value.
     *
     * <p>The value should already be in the appropriate form, i.e.
     * {@linkplain FieldValueChecker checked} and {@linkplain FieldValueParser parsed}.
     *
     * @param value
     *         the name of the variable with the value to be set
     * @see #mergeFormat()
     */
    private void merge(String value) {
        String setterFormat = mergeFormat();
        String setValue = format(setterFormat, value);
        jsGenerator.addLine(setValue);
    }

    /**
     * Returns the format of the set/add action which can be used to merge the field value from
     * the JS variable.
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
        private FieldValueChecker checker;
        private FieldValueParser parser;
        private JsGenerator jsGenerator;

        B setField(FieldDescriptor field) {
            this.field = field;
            return self();
        }

        B setChecker(FieldValueChecker checker) {
            this.checker = checker;
            return self();
        }

        B setParser(FieldValueParser parser) {
            this.parser = parser;
            return self();
        }

        B setJsGenerator(JsGenerator jsGenerator) {
            this.jsGenerator = jsGenerator;
            return self();
        }

        /**
         * <strong>Must</strong> return {@code this} in classes-descendants.
         */
        abstract B self();

        abstract AbstractFieldHandler build();
    }
}
