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

import com.google.common.base.Joiner;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.StringValue;
import io.spine.type.TypeName;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static io.spine.option.OptionsProto.enrichment;
import static io.spine.option.RawListParser.getValueSeparator;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmytro Grankin
 */
public class TypeNamesParserShould {

    private static final String PACKAGE_PREFIX = "foo.bar.";
    private static final String MESSAGE_NAME = "AMessage";

    private final OptionParser<DescriptorProto, TypeName> parser = new TypeNamesParser(enrichment,
                                                                                       PACKAGE_PREFIX);

    @Test
    public void return_empty_collection_if_option_is_not_present() {
        final DescriptorProto definitionWithoutOption = StringValue.getDescriptor()
                                                                   .toProto();
        final Collection<TypeName> parsedTypes = parser.parse(definitionWithoutOption);
        assertTrue(parsedTypes.isEmpty());
    }

    @SuppressWarnings("ConstantConditions") // Purpose of the test.
    @Test(expected = IllegalArgumentException.class)
    public void not_allow_null_option_value() {
        final String nullStr = null;
        parser.parse(nullStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void not_allow_empty_option_value() {
        parser.parse("");
    }

    @Test
    public void split_option_value() {
        final Collection<String> values = asList(MESSAGE_NAME, MESSAGE_NAME);
        final String optionValue = Joiner.on(getValueSeparator())
                                         .join(values);
        final Collection<TypeName> parsedTypes = parser.parse(optionValue);
        assertSimpleNames(values, parsedTypes);
    }

    @Test
    public void add_package_prefix_to_unqualified_type() {
        final Collection<TypeName> parsedTypes = parser.parse(MESSAGE_NAME);
        assertEquals(1, parsedTypes.size());

        final TypeName result = parsedTypes.iterator()
                                           .next();
        assertEquals(PACKAGE_PREFIX + MESSAGE_NAME, result.value());
    }

    @Test
    public void not_add_package_prefix_to_fully_qualified_type() {
        final String fqn = PACKAGE_PREFIX + MESSAGE_NAME;
        final Collection<TypeName> parsedTypes = parser.parse(fqn);
        assertEquals(1, parsedTypes.size());

        final TypeName result = parsedTypes.iterator()
                                           .next();
        assertEquals(fqn, result.value());
    }

    @Test
    public void remove_spaces_from_option_value() {
        final Collection<String> values = asList("TypeOne", "TypeTwo");
        final String spaceBeforeSeparator = ' ' + getValueSeparator();
        final String optionValue = Joiner.on(spaceBeforeSeparator)
                                         .join(values);
        final Collection<TypeName> parsedTypes = parser.parse(optionValue);
        assertSimpleNames(values, parsedTypes);
    }

    @Test(expected = IllegalStateException.class)
    public void not_allow_type_name_consisting_from_spaces() {
        final String invalidName = "   ";
        parser.parse(invalidName);
    }

    private static void assertSimpleNames(Collection<String> expected,
                                          Collection<TypeName> actual) {
        final Iterator<String> expectedIterator = expected.iterator();
        final Iterator<TypeName> actualIterator = actual.iterator();
        while (expectedIterator.hasNext() && actualIterator.hasNext()) {
            final String expectedValue = expectedIterator.next();
            final String actualValue = actualIterator.next()
                                                     .getSimpleName();
            assertEquals(expectedValue, actualValue);
        }
        assertFalse(expectedIterator.hasNext() && actualIterator.hasNext());
    }
}
