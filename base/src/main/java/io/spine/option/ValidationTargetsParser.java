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

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.MessageOptions;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.option.OptionsProto.validationOf;
import static io.spine.option.UnknownOptions.getUnknownOptionValue;
import static java.util.Collections.emptyList;

/**
 * A parser for {@linkplain OptionsProto#validationOf validation rule} targets.
 *
 * @author Dmytro Grankin
 */
public class ValidationTargetsParser extends RawListParser<MessageOptions, DescriptorProto, String> {

    private ValidationTargetsParser() {
        super(validationOf);
    }

    @Override
    public Collection<String> parse(DescriptorProto descriptor) {
        final String value = getUnknownOptionValue(descriptor, getOptionNumber());
        if (value == null) {
            return emptyList();
        }

        return parse(value);
    }

    @Override
    public Collection<String> parse(String optionValue) {
        checkArgument(!isNullOrEmpty(optionValue));
        return splitOptionValue(optionValue);
    }

    /**
     * Obtains the instance of the parser.
     *
     * @return the validation rule parser
     */
    public static ValidationTargetsParser getInstance() {
        return Singleton.INSTANCE.value;
    }

    private enum Singleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final ValidationTargetsParser value = new ValidationTargetsParser();
    }
}
