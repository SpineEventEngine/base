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
import com.google.protobuf.BoolValue;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.StringValue;
import org.junit.Test;

import java.util.Collection;

import static io.spine.option.OptionsProto.enrichment;
import static io.spine.option.RawListParser.getValueSeparator;
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
        final Iterable<String> values = asList(blank, "non-blank");
        final String optionValue = Joiner.on(getValueSeparator())
                                         .join(values);
        parser.parse(optionValue);
    }

    @Test
    public void split_option_value_and_remove_spaces() {
        final Collection<String> result = parser.parse(AListParser.MESSAGE_WITH_OPTION);
        assertEquals(AListParser.EXPECTED_RESULT, result);
    }

    private static class AListParser extends RawListParser<MessageOptions, DescriptorProto, String> {

        /**
         * If this descriptor is passed to {@link RawListParser#parse(GeneratedMessageV3)},
         * {@link #OPTION_VALUE_WITH_SPACES} will be returned.
         */
        private static final DescriptorProto MESSAGE_WITH_OPTION = BoolValue.getDescriptor()
                                                                            .toProto();
        private static final String OPTION_VALUE_WITH_SPACES =
                "   " + OPTION_PART + "  , " + OPTION_PART + "    ";

        /**
         * The expected result for parsing {@link #MESSAGE_WITH_OPTION}.
         */
        private static final Iterable<String> EXPECTED_RESULT = asList(OPTION_PART, OPTION_PART);

        private AListParser(GeneratedExtension<MessageOptions, String> option) {
            super(option);
        }

        @SuppressWarnings("ReturnOfNull") // Contract of the method.
        @Override
        protected String getUnknownOptionValue(DescriptorProto descriptor, int optionNumber) {
            return descriptor.equals(MESSAGE_WITH_OPTION)
                    ? OPTION_VALUE_WITH_SPACES
                    : null;
        }

        @Override
        protected Collection<String> wrapParts(Collection<String> parts) {
            return parts;
        }
    }
}
