/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.validate;

import com.google.protobuf.Descriptors.Descriptor;
import io.spine.option.Options;

import java.util.Optional;

import static io.spine.option.OptionsProto.requiredField;

/**
 * A message option that defines a combination of required fields for the message.
 *
 * The fields are separated with a {@code |} symbol, and combined with a {@code &} symbol.
 *
 * Example:
 * <pre>
 *     {@code
 *     message PersonName {
 *         option (required_field) = "given_name|honorific_prefix & family_name";
 *         string honorific_prefix = 1;
 *         string given_name = 2;
 *         string middle_name = 3;
 *     }
 *     }
 * </pre>
 * The {@code PersonName} message is valid against the {@code RequiredField} either if it has a
 * non-default family name, or both honorific prefix and a family name.
 */
final class RequiredField implements ValidatingOption<String, Descriptor, MessageValue> {

    @Override
    public Optional<String> valueFrom(Descriptor message) {
        Optional<String> result = Options.option(message, requiredField);
        return result;
    }

    @Override
    public Constraint<MessageValue> constraintFor(MessageValue message) {
        String expression = valueFrom(message.descriptor()).orElse("");
        return new RequiredFieldConstraint(expression);
    }
}
