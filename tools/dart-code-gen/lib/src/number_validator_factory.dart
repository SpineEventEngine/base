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

const _numericRange = r'([\[(])\s*([+\-]?[\d.]+)\s*\.\.\s*([+\-]?[\d.]+)\s*([\])])';

/// A [FieldValidatorFactory] for number fields.
///
/// Supports options `(min)`, `(max)`, and `(range)`.
///
class NumberValidatorFactory<N extends num> extends SingularFieldValidatorFactory {
    
    final String _wrapperType;

    NumberValidatorFactory(ValidatorFactory validatorFactory,
                           FieldDescriptorProto field,
                           this._wrapperType)
        : super(validatorFactory, field);

    N _parse(String value) => _doParse(value.trim());

    N _doParse(String value) => null;
    
    @override
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
        if (options.hasExtension(Options.range)) {
            Iterable<Rule> range = _rangeRules(options);
            rules.addAll(range);
        }
        return rules;
    }

    /// Numbers can neither be `(required)` nor be a part of `(required_field)`.
    ///
    /// Protobuf does not distinguish between the default value of `0` and the domain value of `0`,
    /// hence it's not possible to tell if a number field is set or not.
    ///
    bool supportsRequired() => false;

    Rule _minRule(FieldOptions options) {
        var min = options.getExtension(Options.min) as MinOption;
        var bound = _parse(min.value);
        var exclusive = min.exclusive;
        return _constructMinRule(bound, exclusive);
    }
    
    Rule _constructMinRule(N bound, bool exclusive) {
        var literal = literalNum(bound);
        var check = exclusive
                    ? (Expression v) => v.lessOrEqualTo(literal)
                    : (Expression v) => v.lessThan(literal);
        var requiredString = newRule((v) => check(v), _outOfBound);
        return requiredString;
    }

    Rule _maxRule(FieldOptions options) {
        var max = options.getExtension(Options.max) as MaxOption;
        var bound = _parse(max.value);
        var exclusive = max.exclusive;
        return _constructMaxRule(bound, exclusive);
    }

    Rule _constructMaxRule(N bound, bool exclusive) {
      var literal = literalNum(bound);
      var check = exclusive
                  ? (Expression v) => v.greaterOrEqualTo(literal)
                  : (Expression v) => v.greaterThan(literal);
      var rule = newRule((v) => check(v), _outOfBound);
      return rule;
    }
    
    Iterable<Rule> _rangeRules(FieldOptions options) {
        var rangeNotation = options.getExtension(Options.range) as String;
        var rangePattern = RegExp(_numericRange);
        var match = rangePattern.firstMatch(rangeNotation.trim());
        if (match == null) {
            throw ArgumentError('Malformed range: `$rangeNotation`. '
                                'See doc of (range) for the proper format.');
        }
        var startOpen = match.group(1) == '(';
        var start = _parse(match.group(2));
        var end = _parse(match.group(3));
        var endOpen = match.group(4) == ')';

        var minRule = _constructMinRule(start, startOpen);
        var maxRule = _constructMaxRule(end, endOpen);
        return [minRule, maxRule];
    }

    // TODO:2019-10-14:dmytro.dashenkov: Support custom error messages based on the option value.
    // https://github.com/SpineEventEngine/base/issues/482
    Expression _outOfBound(Expression value) {
        var param = 'v';
        var standardPackage = validatorFactory.properties.standardPackage;
        var floatValue = refer(_wrapperType, protoWrappersImport(standardPackage))
            .newInstance([])
            .property('copyWith')
            .call([Method((b) => b
            ..requiredParameters.add(Parameter((b) => b.name = param))
            ..body = refer(param)
                .property('value')
                .assign(value)
                .statement).closure]);
        var any = refer('Any', protoAnyImport(standardPackage)).property('pack').call([floatValue]);
        return violationRef.call([literalString('Number is out of bound.'),
                                  literalString(validatorFactory.fullTypeName),
                                  literalList([field.name]),
                                  any]);
    }
}

/// A [NumberValidatorFactory] for non-integer numbers.
///
class DoubleValidatorFactory extends NumberValidatorFactory<double> {

    DoubleValidatorFactory._(ValidatorFactory validatorFactory,
                             FieldDescriptorProto field,
                             String wrapperType)
        : super(validatorFactory, field, wrapperType);

    /// Creates a new validator factory for a `float` field.
    factory DoubleValidatorFactory.forFloat(ValidatorFactory validatorFactory,
                                            FieldDescriptorProto field) {
        return DoubleValidatorFactory._(validatorFactory, field, 'FloatValue');
    }

    /// Creates a new validator factory for a `double` field.
    factory DoubleValidatorFactory.forDouble(ValidatorFactory validatorFactory,
                                             FieldDescriptorProto field) {
        return DoubleValidatorFactory._(validatorFactory, field, 'DoubleValue');
    }

    @override
    double _doParse(String value) => double.parse(value);
}

/// A [NumberValidatorFactory] for integer numbers.
///
class IntValidatorFactory extends NumberValidatorFactory<int> {

    IntValidatorFactory._(ValidatorFactory validatorFactory,
                          FieldDescriptorProto field,
                          String wrapperType)
        : super(validatorFactory, field, wrapperType);

    /// Creates a new validator factory for a signed 32-bit integer.
    ///
    /// `int32`, `sint32`, `fixed32`, and `sfixed32` all should be validated via the code generated
    /// by this factory.
    ///
    factory IntValidatorFactory.forInt32(ValidatorFactory validatorFactory,
                                         FieldDescriptorProto field) {
        return IntValidatorFactory._(validatorFactory, field, 'Int32Value');
    }

    /// Creates a new validator factory for a signed 64-bit integer.
    ///
    /// `int64`, `sint64`, `fixed64`, and `sfixed64` all should be validated via the code generated
    /// by this factory.
    ///
    factory IntValidatorFactory.forInt64(ValidatorFactory validatorFactory,
                                         FieldDescriptorProto field) {
        return IntValidatorFactory._(validatorFactory, field, 'Int64Value');
    }

    /// Creates a new validator factory for a unsigned 32-bit integer.
    factory IntValidatorFactory.forUInt32(ValidatorFactory validatorFactory,
                                          FieldDescriptorProto field) {
        return IntValidatorFactory._(validatorFactory, field, 'UInt32Value');
    }

    /// Creates a new validator factory for a unsigned 64-bit integer.
    factory IntValidatorFactory.forUInt64(ValidatorFactory validatorFactory,
                                          FieldDescriptorProto field) {
        return IntValidatorFactory._(validatorFactory, field, 'UInt64Value');
    }

    @override
    int _doParse(String value) => int.parse(value);
}
