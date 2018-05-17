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

package io.spine.validate.rules;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import io.spine.option.OptionsProto;
import io.spine.option.RawListValue;
import io.spine.option.UnknownOptions;

import static io.spine.option.OptionsProto.validationOf;

/**
 * A parser for {@linkplain OptionsProto#validationOf validation rule} targets.
 *
 * @author Dmytro Grankin
 */
class ValidationTargetValue extends RawListValue<MessageOptions, DescriptorProto, String> {

    private ValidationTargetValue() {
        super(validationOf);
    }

    @Override
    protected String get(DescriptorProto descriptor) {
        return UnknownOptions.get(descriptor, getOptionNumber());
    }

    /**
     * Obtains the parsed element from the specified value.
     *
     * <p>This class has not specific requirements, so the specified parameter will be returned.
     *
     * @param singleItemValue the item from the option value
     * @return the parsed value
     */
    @Override
    protected String asElement(String singleItemValue) {
        return singleItemValue;
    }

    /**
     * Obtains the instance of the parser.
     *
     * @return the validation rule parser
     */
    public static ValidationTargetValue getInstance() {
        return Singleton.INSTANCE.value;
    }

    private enum Singleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final ValidationTargetValue value = new ValidationTargetValue();
    }
}
