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

package io.spine.tools.validate.field;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import io.spine.code.proto.FieldDeclaration;
import io.spine.option.PatternOption;
import io.spine.tools.validate.ViolationTemplate;
import io.spine.tools.validate.code.Expression;

import java.util.function.Function;

import static io.spine.option.OptionsProto.pattern;
import static io.spine.option.OptionsProto.required;
import static io.spine.tools.validate.code.Expression.formatted;
import static java.lang.String.format;

public final class StringFieldValidatorFactory extends AbstractFieldValidatorFactory {

    StringFieldValidatorFactory(FieldDeclaration field, Expression fieldAccess) {
        super(field, fieldAccess);
    }

    @Override
    protected ImmutableList<Rule> rules() {
        ImmutableList.Builder<Rule> builder = ImmutableList.builder();
        FieldOptions options = field().descriptor().getOptions();
        if (options.getExtension(required)) {
            builder.add(required());
        }
        if (options.hasExtension(pattern)) {
            PatternOption option = options.getExtension(pattern);
            builder.add(pattern(option));
        }
        return builder.build();
    }

    private Rule required() {
        Function<Expression, Expression> condition =
                field -> formatted("%s.isEmpty()", field);
        @SuppressWarnings("DuplicateStringLiteralInspection") // Duplicates are in generated code.
        Function<Expression, ViolationTemplate> violationFactory =
                field -> violationTemplate()
                        .setMessage("Field must be set.")
                        .build();
        return new Rule(
                condition,
                violationFactory
        );
    }

    private Rule pattern(PatternOption pattern) {
        String regex = pattern.getRegex();
        Function<Expression, Expression> condition =
                field -> formatted("%s.matches(\"%s\")", field, regex);
        Function<Expression, ViolationTemplate> violationFactory =
                field -> violationTemplate()
                        .setMessage(format("String must match %s.", regex))
                        .build();
        return new Rule(
                condition,
                violationFactory
        );
    }
}
