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

import 'package:code_builder/code_builder.dart';
import 'package:dart_code_gen/google/protobuf/descriptor.pb.dart';
import 'package:dart_code_gen/src/field_validator_factory.dart';
import 'package:dart_code_gen/src/validator_factory.dart';

import 'field_validator_factory.dart';
import 'validator_factory.dart';

/// Non-default enum constants start at this number.
const _minNonEmptyEnumValue = 1;

/// A [FieldValidatorFactory] for `bytes` fields.
///
class EnumValidatorFactory extends SingularFieldValidatorFactory {

    EnumValidatorFactory(ValidatorFactory validatorFactory, FieldDescriptorProto field)
        : super(validatorFactory, field);

    @override
    Iterable<Rule> rules() {
        var rules = <Rule>[];
        if (isRequired()) {
            rules.add(createRequiredRule());
        }
        return rules;
    }

    @override
    LazyCondition notSetCondition() =>
            (v) => v.property('value').lessThan(literalNum(_minNonEmptyEnumValue));
}
