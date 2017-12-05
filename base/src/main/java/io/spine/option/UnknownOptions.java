/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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
package io.spine.option;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.UnknownFieldSet.Field;
import io.spine.annotation.Internal;

import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyMap;

/**
 * A utility class which helps to get "unknown" Protobuf option field numbers
 * (as in option type declaration) and option values.
 *
 * <p>For example, a map with pairs:
 *
 * <p>{@code 50123 -> "option_string_value_1"}
 * <p>{@code 50124 -> "option_string_value_2"}
 *
 * <p>An option is "unknown" and serialized if there is no dependency on the artifact
 * which contains the needed option definition.
 * For example, we should not depend on "Spine/core-java" project artifacts to avoid
 * circular dependency.
 *
 * <p>There can be several option properties, and several same numbers in the options string:
 *
 * [(decimal_max).value = "64.5", (decimal_max).inclusive = true];
 *
 * <p>Currently, we do not need options with several properties.
 * So, only the first option property values are obtained, and others are ignored.
 *
 * @author Alexander Litus
 */
@Internal
public class UnknownOptions {

    @SuppressWarnings("HardcodedLineSeparator")
    private static final Pattern PATTERN_NEW_LINE = Pattern.compile("\n");
    private static final Pattern PATTERN_COLON = Pattern.compile(":");
    private static final String QUOTE = "\"";

    private UnknownOptions() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Obtains a map from "unknown" Protobuf option field numbers to option values.
     *
     * @param file the file descriptor, from which to get options
     * @return a file option number to an option value map
     */
    public static Map<Integer, String> getUnknownOptions(FileDescriptorProto file) {
        final UnknownFieldSet unknownFields = file.getOptions()
                                                  .getUnknownFields();
        final Map<Integer, String> result = extractOptions(unknownFields);
        return result;
    }

    /**
     * Tells whether the message is marked with an option which has the given number
     * in its definition.
     *
     * @param message             the message descriptor to check the option presence
     * @param messageOptionNumber the option number for the option to check
     * @return {@code} true if the message has the option
     */
    public static boolean hasUnknownOption(DescriptorProto message, int messageOptionNumber) {
        final UnknownFieldSet unknownFields = message.getOptions()
                                                     .getUnknownFields();
        return containsField(unknownFields, messageOptionNumber);
    }

    /**
     * Obtains a string value of "unknown" Protobuf option or {@code null} if no option
     * with such field number found.
     *
     * @param file              the file descriptor, from which to get the option
     * @param optionFieldNumber the requested option field number
     * @return a string representation of the option
     */
    public static String getUnknownOptionValue(FileDescriptorProto file, int optionFieldNumber) {
        final Map<Integer, String> options = getUnknownOptions(file);
        final String result = options.get(optionFieldNumber);
        return result;
    }

    /**
     * Obtains a map from "unknown" Protobuf option field numbers to option values.
     *
     * @param message the message descriptor, from which to get options
     * @return a message option number to an option value map
     */
    public static Map<Integer, String> getUnknownOptions(DescriptorProto message) {
        final UnknownFieldSet unknownFields = message.getOptions()
                                                     .getUnknownFields();
        final Map<Integer, String> result = extractOptions(unknownFields);
        return result;
    }

    /**
     * Obtains a string value of "unknown" Protobuf option or {@code null} if there is no option
     * with the specified field number found.
     *
     * @param enumDescriptor    the enum descriptor, from which to get the option
     * @param optionFieldNumber the requested option field number
     * @return a string representation of the option
     */
    public static String getUnknownOptionValue(EnumDescriptorProto enumDescriptor,
                                               int optionFieldNumber) {
        final Map<Integer, String> options = getUnknownOptions(enumDescriptor);
        final String result = options.get(optionFieldNumber);
        return result;
    }

    /**
     * Obtains a map from "unknown" Protobuf enum option numbers to option values.
     *
     * @param enumDescriptor the enum descriptor, from which to get options
     * @return an enum option number to an option value map
     */
    public static Map<Integer, String> getUnknownOptions(EnumDescriptorProto enumDescriptor) {
        final UnknownFieldSet unknownFields = enumDescriptor.getOptions()
                                                            .getUnknownFields();
        final Map<Integer, String> result = extractOptions(unknownFields);
        return result;
    }

    /**
     * Obtains a string value of "unknown" Protobuf option or {@code null} if no option
     * with such field number found.
     *
     * @param msg               the message descriptor, from which to get the option
     * @param optionFieldNumber the requested option field number
     * @return a string representation of the option
     */
    public static String getUnknownOptionValue(DescriptorProto msg, int optionFieldNumber) {
        final Map<Integer, String> options = getUnknownOptions(msg);
        final String result = options.get(optionFieldNumber);
        return result;
    }

    /**
     * Obtains a map from "unknown" Protobuf option field numbers to option values.
     *
     * @param field the field descriptor, from which to get options
     * @return an field option number to an option value map
     */
    public static Map<Integer, String> getUnknownOptions(FieldDescriptorProto field) {
        final UnknownFieldSet unknownFields = field.getOptions()
                                                   .getUnknownFields();
        final Map<Integer, String> result = extractOptions(unknownFields);
        return result;
    }

    /**
     * Obtains a string value of "unknown" Protobuf option or {@code null} if there is no option
     * with the specified field number found.
     *
     * @param service           the service descriptor, from which to get options
     * @param optionFieldNumber the requested option field number
     * @return a string representation of option
     */
    public static String getUnknownOptionValue(ServiceDescriptorProto service,
                                               int optionFieldNumber) {
        final Map<Integer, String> options = getUnknownOptions(service);
        final String result = options.get(optionFieldNumber);
        return result;
    }

    /**
     * Obtains a map from "unknown" Protobuf service option numbers to option values.
     *
     * @param service the service descriptor, from which to get options
     * @return a service option number to an option value map
     */
    public static Map<Integer, String> getUnknownOptions(ServiceDescriptorProto service) {
        final UnknownFieldSet unknownFields = service.getOptions()
                                                     .getUnknownFields();

        final Map<Integer, String> result = extractOptions(unknownFields);
        return result;
    }

    /**
     * Tells whether the field is marked with an option which has the given field number
     * in its definition.
     *
     * @param field             the field descriptor to check the option presence
     * @param optionFieldNumber the option number for the option to check
     * @return {@code} true if the field has the option
     */
    public static boolean hasUnknownOption(FieldDescriptorProto field, int optionFieldNumber) {
        final UnknownFieldSet unknownFields = field.getOptions()
                                                   .getUnknownFields();
        return containsField(unknownFields, optionFieldNumber);
    }

    /**
     * Obtains a string value of "unknown" Protobuf option or {@code null} if no option with
     * such field number found.
     *
     * @param field             the field descriptor, from which to get the option
     * @param optionFieldNumber the requested option field number
     * @return a string representation of the option
     */
    public static String getUnknownOptionValue(FieldDescriptorProto field, int optionFieldNumber) {
        final Map<Integer, String> options = getUnknownOptions(field);
        final String result = options.get(optionFieldNumber);
        return result;
    }

    /**
     * Checks if the is a field with such {@code tag} in the given {@code unknownFields} set.
     *
     * @param unknownFields the {@link UnknownFieldSet} to check
     * @param tag           the field number (tag) to look for
     * @return {@code true} if the value is present in the unknown fields set, {@code false}
     *         otherwise
     */
    private static boolean containsField(UnknownFieldSet unknownFields, int tag) {
        final Map<Integer, ?> fields = unknownFields.asMap();
        final boolean result = fields.containsKey(tag);
        return result;
    }

    /**
     * Extracts the values of unknown options of a message as a {@code Map} of the field number to
     * it's string representation.
     *
     * @param unknownFields the {@link UnknownFieldSet} to extract the values from
     * @return a map of the field numbers to the field values
     */
    private static Map<Integer, String> extractOptions(UnknownFieldSet unknownFields) {
        final Map<Integer, Field> fields = unknownFields.asMap();
        if (fields.isEmpty()) {
            return emptyMap();
        }
        final String rawFields = unknownFields.toString();
        final Map<Integer, String> result = unsafeParseOptions(rawFields);
        return result;
    }

    /**
     * Parses the given string representation of the Protobuf unknown options.
     *
     * <p>This method expects the passed string to adhere a certain protocol. It's not recommended
     * to call it directly (even within {@code UnknownOptions} class).
     *
     * @param optionsStr the string to parse
     * @return the map of the parsed "unknown" options
     * @see #extractOptions(UnknownFieldSet) extractOptions(UnknownFieldSet) for a safer way
     * to extract the options
     */
    private static Map<Integer, String> unsafeParseOptions(String optionsStr) {
        if (optionsStr.trim()
                      .isEmpty()) {
            return emptyMap();
        }
        final Map<Integer, String> map = newHashMap();
        final String[] options = PATTERN_NEW_LINE.split(optionsStr);
        for (String option : options) {
            if (PATTERN_COLON.matcher(option)
                             .find()) {
                parseAndPutNumberAndValue(option, map);
            }
        }
        return ImmutableMap.copyOf(map);
    }

    private static void parseAndPutNumberAndValue(String option, Map<Integer, String> map) {
        // we need only two parts split by the first colon
        final int limit = 2;
        final String[] numberAndValue = PATTERN_COLON.split(option, limit);
        final String numberStr = numberAndValue[0].trim();
        final int number = Integer.valueOf(numberStr);
        String value = numberAndValue[1].trim();
        if (value.startsWith(QUOTE) && value.endsWith(QUOTE)) {
            value = value.substring(1, value.length() - 1);
        }
        // Check for duplicates to avoid exceptions on immutable map creation.
        // See the class docs for details.
        if (!map.containsKey(number)) {
            map.put(number, value);
        }
    }
}
