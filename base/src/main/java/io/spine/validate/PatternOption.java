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

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.option.OptionsProto;

import java.util.Optional;

/**
 * An option that defines a pattern that a field value has to match.
 */
public class PatternOption extends FieldValidatingOption<io.spine.option.PatternOption, String> {

    private PatternOption() {
    }

    /** Returns a new instance of this option. */
    public static PatternOption create() {
        return new PatternOption();
    }

    private static io.spine.option.PatternOption getOption(FieldValue<String> fieldValue) {
        io.spine.option.PatternOption option = fieldValue.valueOf(OptionsProto.pattern);
        return option;
    }

    @Override
    public Optional<io.spine.option.PatternOption> valueFrom(FieldValue<String> bearer) {
        io.spine.option.PatternOption regex = bearer.valueOf(OptionsProto.pattern);
        return bearer.valueOf(OptionsProto.pattern)
                     .getRegex()
                     .isEmpty() ?
               Optional.empty() :
               Optional.of(regex);
    }

    @Override
    Constraint<FieldValue<String>> constraint() {
        return new PatternConstraint();
    }

    @Override
    GeneratedExtension<FieldOptions, io.spine.option.PatternOption> optionExtension() {
        return OptionsProto.pattern;
    }
}
