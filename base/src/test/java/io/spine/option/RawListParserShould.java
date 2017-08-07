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
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.StringValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static io.spine.option.OptionsProto.enrichment;
import static io.spine.option.RawListParser.getValueSeparator;
import static io.spine.option.UnknownOptions.getUnknownOptionValue;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RawListParserShould {

    private static final String OPTION_PART = "PART";

    private final OptionParser<DescriptorProto, String> parser = new AListParser(enrichment);

    @Test
    public void return_empty_collection_if_option_is_not_present() {
        final DescriptorProto definitionWithoutOption = StringValue.getDescriptor()
                                                                   .toProto();
        final Collection<String> result = parser.parse(definitionWithoutOption);
        assertTrue(result.isEmpty());
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

    @Test(expected = IllegalStateException.class)
    public void not_allow_blank_value() {
        final String blank = "   ";
        final Iterable<String> values = Arrays.asList(blank, "non-blank");
        final String optionValue = Joiner.on(getValueSeparator())
                                         .join(values);
        parser.parse(optionValue);
    }

    @Test
    public void split_option_value_and_remove_spaces() {
        final Collection<String> expectedValues = asList(OPTION_PART, OPTION_PART);
        final String spaceBeforeSeparator = ' ' + getValueSeparator();
        final String optionValue = Joiner.on(spaceBeforeSeparator)
                                         .join(expectedValues);
        final Collection<String> parsedValues = parser.parse(optionValue);
        assertEquals(expectedValues, parsedValues);
    }

    private static class AListParser extends RawListParser<MessageOptions, DescriptorProto, String> {

        private AListParser(GeneratedExtension<MessageOptions, String> option) {
            super(option);
        }

        @Override
        protected String getOptionValue(DescriptorProto descriptor, int optionNumber) {
            return getUnknownOptionValue(descriptor, optionNumber);
        }

        @Override
        protected Collection<String> wrapParts(Collection<String> parts) {
            return parts;
        }
    }
}
