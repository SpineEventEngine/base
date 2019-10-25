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
import 'package:dart_code_gen/spine/options.pb.dart';

import 'constraint_violation.dart';
import 'field_validator_factory.dart';
import 'validator_factory.dart';

/// A [FieldValidatorFactory] for `string` fields.
///
/// The only supported options is `(required)` and `(pattern)`.
///
class StringValidatorFactory extends SingularFieldValidatorFactory {

    StringValidatorFactory(ValidatorFactory validatorFactory, FieldDescriptorProto field)
        : super(validatorFactory, field);

    @override
    Iterable<Rule> rules() {
        var options = field.options;
        var rules = <Rule>[];
        if (isRequired()) {
            rules.add(createRequiredRule());
        }
        if (options.hasExtension(Options.pattern)) {
            Rule rule = _patternRule(options);
            rules.add(rule);
        }
        return rules;
    }

    @override
    LazyCondition notSetCondition() => (v) => v.property('isEmpty');

    /// Creates a validation rule which matches a string upon a regular expression.
    ///
    /// The whole string must match the regex. To check that, the generated code applies
    /// the `RegExp` to the string and compares the first match to the value of the initial string.
    /// If the values are identical, the check passes.
    ///
    Rule _patternRule(FieldOptions options) {
        PatternOption pattern = options.getExtension(Options.pattern);
        var rule = newRule((v) => refer('RegExp')
            .newInstance([literalString(pattern.regex, raw: true)])
            .property('stringMatch')
            .call([v])
            .notEqualTo(v),
                           (v) => _patternMismatch(pattern.regex));
        return rule;
    }

    Expression _patternMismatch(String pattern) {
        var message = 'String must match the regular expression `$pattern`';
        return violationRef.call([literalString(message, raw: true),
                                  literalString(validatorFactory.fullTypeName),
                                  literalList([field.name])]);
    }
}
