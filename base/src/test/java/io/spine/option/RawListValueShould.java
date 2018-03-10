/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
import org.junit.Test;

import java.util.Collection;

import static io.spine.option.OptionsProto.enrichment;
import static io.spine.option.RawListValue.getValueSeparator;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class RawListValueShould {

    private static final String OPTION_PART = "PART";

    private final OptionValue<String> parser = new AListValue(enrichment);

    @SuppressWarnings("ConstantConditions") // Purpose of the test.
    @Test(expected = NullPointerException.class)
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
        final Collection<String> expectedValues = asList(OPTION_PART, OPTION_PART);
        final String spacesBeforeSeparator = "   " + getValueSeparator();
        final String optionValue = Joiner.on(spacesBeforeSeparator)
                                         .join(expectedValues);
        final Collection<String> parsedValues = parser.parse(optionValue);
        assertEquals(expectedValues, parsedValues);
    }

    private static class AListValue extends RawListValue<MessageOptions, DescriptorProto, String> {

        private AListValue(GeneratedExtension<MessageOptions, String> option) {
            super(option);
        }

        @Override
        protected String get(DescriptorProto descriptor) {
            return UnknownOptions.get(descriptor, getOptionNumber());
        }

        @Override
        protected String asElement(String singleItemValue) {
            return singleItemValue;
        }
    }
}
