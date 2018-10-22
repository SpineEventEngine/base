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

package io.spine.tools.compiler.validation;

import com.google.template.soy.SoyFileSet;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import com.squareup.javapoet.ClassName;
import io.spine.code.proto.FieldName;

import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.String.format;

/**
 * Utility class for working with {@code MethodConstructor}s.
 *
 * @author Illia Shepilov
 */
final class MethodConstructors {

    private static final String TEMPLATE_PATH = "templates/method_constructors.soy";

    private static final SoyTofu soyTofu = getSoyTofu();

    /** Prevents instantiation of this utility class. */
    private MethodConstructors() {
    }

    /**
     * Creates the descriptor statement.
     *
     * @param index            the index of the field to validate
     * @param messageClassName the message class which contains field to validate
     * @return the constructed statement
     */
    static String createDescriptorStatement(int index, ClassName messageClassName) {
        checkNotNull(messageClassName);
        SoyMapData mapData = new SoyMapData("fieldIndex", index,
                                            "messageClassName", messageClassName.toString());
        String result = renderData(mapData, "io.spine.generation.descriptorStatement");
        return result;
    }

    /**
     * Creates the validate statement.
     *
     * @param fieldValue the value to validate
     * @return the constructed statement
     */
    static String createValidateStatement(String fieldValue) {
        checkNotNull(fieldValue);
        SoyMapData mapData = new SoyMapData("fieldValue", fieldValue);
        String result = renderData(mapData, "io.spine.generation.validateStatement");
        return result;
    }

    /**
     * Creates the statement which returns the converted value.
     *
     * @param value the value to convert
     * @return the constructed statement
     */
    static String createConvertSingularValue(String value) {
        checkNotNull(value);
        // We pass capitalized name because this value is used with prefixes.
        String fieldName = FieldName.of(value)
                                    .toCamelCase();
        SoyMapData mapData = new SoyMapData("javaFieldName", fieldName,
                                            "valueToValidate", value);
        String result = renderData(mapData, "io.spine.generation.convertedValueStatement");
        return result;
    }

    /**
     * Returns the suffix for the `raw` methods of the `ValidatingBuilder` classes.
     *
     * @return the {@code String} which represents the raw suffix
     */
    static String rawSuffix() {
        return "Raw";
    }

    /**
     * Returns the prefix for the `clear` methods of the `ValidatingBuilder` classes.
     *
     * @return the {@code String} which represents the prefix for the `clear` methods
     */
    static String clearPrefix() {
        return "clear";
    }

    /**
     * Returns the prefix for the `remove` methods of the `ValidatingBuilder` classes.
     *
     * @return the {@code String} which represents the prefix for the `remove` methods
     */
    static String removePrefix() {
        return "remove";
    }

    /**
     * Returns the `return` statement for the methods of the `ValidatingBuilder` classes.
     *
     * @return the {@code String} which represents the `return` statement
     */
    static String returnThis() {
        return "return this";
    }

    /**
     * Creates the {@code ... .clearProperty()} statement for the given property name.
     *
     * @param propertyName the name of the property to clear
     * @return the {@code String} representing the clear call
     */
    static String clearProperty(String propertyName) {
        checkNotNull(propertyName);
        checkState(!propertyName.isEmpty());

        return format(".clear%s()", propertyName);
    }

    /**
     * Returns the getter code fragment of the predefined {@code Message.Builder}.
     *
     * @return the {@code String} which represents the pointer
     */
    static String getMessageBuilder() {
        return "getMessageBuilder()";
    }

    private static String renderData(SoyMapData mapData, String templateName) {
        String result = soyTofu.newRenderer(templateName)
                               .setData(mapData)
                               .render();
        return result;
    }

    private static SoyTofu getSoyTofu() {
        URL resource = MethodConstructors.class.getClassLoader()
                                               .getResource(TEMPLATE_PATH);
        if (resource == null) {
            String exMessage = format("The template file %s is not found.", TEMPLATE_PATH);
            throw newIllegalStateException(exMessage);
        }

        SoyFileSet sfs = SoyFileSet.builder()
                                   .add(resource)
                                   .build();
        return sfs.compileToTofu();
    }
}
