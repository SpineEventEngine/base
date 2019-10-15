
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
import 'imports.dart';
import 'validator_factory.dart';

/// A [FieldValidatorFactory] for `float` fields.
///
/// Currently, the supported options are `(min)` and `(max)`.
///
class FloatValidatorFactory extends FieldValidatorFactory {

    FloatValidatorFactory(ValidatorFactory validatorFactory, FieldDescriptorProto field)
        : super(validatorFactory, field);

    Iterable<Rule> rules() {
        var rules = <Rule>[];
        var options = field.options;
        if (options.hasExtension(Options.min)) {
            Rule min = _minRule(options);
            rules.add(min);
        }
        if (options.hasExtension(Options.max)) {
            Rule max = _maxRule(options);
            rules.add(max);
        }
        return rules;
    }

    Rule _minRule(FieldOptions options) {
        var min = options.getExtension(Options.min) as MinOption;
        var bound = double.parse(min.value);
        var exclusive = min.exclusive;
        var literal = literalNum(bound);
        var check = exclusive
                    ? (Expression v) => v.lessOrEqualTo(literal)
                    : (Expression v) => v.lessThan(literal);
        var requiredString = newRule((v) => check(v), _outOfBound);
        return requiredString;
    }

    Rule _maxRule(FieldOptions options) {
        var max = options.getExtension(Options.max) as MaxOption;
        var bound = double.parse(max.value);
        var exclusive = max.exclusive;
        var literal = literalNum(bound);
        var check = exclusive
                    ? (Expression v) => v.greaterOrEqualTo(literal)
                    : (Expression v) => v.greaterThan(literal);
        var requiredString = newRule((v) => check(v), _outOfBound);
        return requiredString;
    }

    // TODO:2019-10-14:dmytro.dashenkov: Support custom error messages based on the option value.
    // https://github.com/SpineEventEngine/base/issues/482
    Expression _outOfBound(Expression value) {
        var param = 'v';
        var standardPackage = validatorFactory.properties.standardPackage;
        var floatValue = refer('FloatValue', protoWrappersImport(standardPackage))
            .newInstance([])
            .property('copyWith')
            .call([Method((b) => b
            ..requiredParameters.add(Parameter((b) => b.name = param))
            ..body = refer(param)
                .property('value')
                .assign(value)
                .statement).closure]);
        var any = refer('Any', protoAnyImport(standardPackage)).property('pack').call([floatValue]);
        return violationRef.call([literalString('Float field is out of bound.'),
                                     literalString(validatorFactory.fullTypeName),
                                     literalList([field.name]),
                                     any]);
    }
}
