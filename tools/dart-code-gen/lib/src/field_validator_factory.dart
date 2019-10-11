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
import 'imports.dart';
import 'validator_factory.dart';

FieldValidatorFactory forField(FieldDescriptorProto field, ValidatorFactory factory) {
    var type = field.type;
    switch (type) {
        case FieldDescriptorProto_Type.TYPE_STRING:
            return new _StringValidatorFactory(factory, field);
        case FieldDescriptorProto_Type.TYPE_FLOAT:
            return new _FloatValidatorFactory(factory, field);
    }
    return null;
}

class FieldValidatorFactory {

    final ValidatorFactory validatorFactory;
    final FieldDescriptorProto field;

    FieldValidatorFactory(this.validatorFactory, this.field);

    Code createFieldValidator(Expression fieldValue) {
        var statements = _rules()
            .map((r) => r.eval(fieldValue))
            .map((expression) => expression.statement)
            .toList();
        return statements.isNotEmpty
               ? Block.of(statements)
               : null;
    }

    Iterable<_Rule> _rules() => null;

    Expression _requiredMissing() {
        return violationRef.call([literalString('Field must be set.'),
                                  literalString(validatorFactory.fullName),
                                  literalList([field.name])]);
    }
}

class _Rule {

    final LazyCondition _condition;
    final LazyViolation _violation;
    final Expression _targetViolationList;

    _Rule(this._condition, this._violation, this._targetViolationList);

    /// Produces a ternary operator which creates a new violation if the string is empty.
    ///
    /// ```dart
    /// validationCondition(<Field value>)
    ///     ? violations.add(_violation(..))
    ///     : null;
    /// ```
    ///
    /// `code_builder` does not support `if` statements, so a ternary conditional operator has to
    /// be used.
    ///
    Expression eval(Expression fieldValue) {
        var ternaryOperator = _condition(fieldValue).conditional(
            _targetViolationList
                .property('add')
                .call([_violation(fieldValue)]),
            literalNull);
        return ternaryOperator;
    }
}

typedef Expression LazyViolation(Expression fieldValue);
typedef Expression LazyCondition(Expression fieldValue);

class _StringValidatorFactory extends FieldValidatorFactory {

    _StringValidatorFactory(ValidatorFactory validatorFactory, FieldDescriptorProto field)
        : super(validatorFactory, field);

    Iterable<_Rule> _rules() {
        var rules = <_Rule>[];
        if (field.options.hasExtension(Options.required)) {
            var requiredString = _Rule((v) => v.property('isEmpty'),
                                       (v) => _requiredMissing(),
                                       validatorFactory.violationList);
            rules.add(requiredString);
        }
        return rules;
    }
}

class _FloatValidatorFactory extends FieldValidatorFactory {

    _FloatValidatorFactory(ValidatorFactory validatorFactory, FieldDescriptorProto field)
        : super(validatorFactory, field);

    Iterable<_Rule> _rules() {
        var rules = <_Rule>[];
        var options = field.options;
        if (options.hasExtension(Options.min)) {
            var min = options.getExtension(Options.min) as MinOption;
            var bound = double.parse(min.value);
            var exclusive = min.exclusive;
            var literal = literalNum(bound);
            var check = exclusive
                        ? (Expression v) => v.lessOrEqualTo(literal)
                        : (Expression v) => v.lessThan(literal);
            var requiredString = _Rule((v) => check(v),
                                       _outOfBound,
                                       validatorFactory.violationList);
            rules.add(requiredString);
        }
        if (options.hasExtension(Options.max)) {
            var max = options.getExtension(Options.max) as MaxOption;
            var bound = double.parse(max.value);
            var exclusive = max.exclusive;
            var literal = literalNum(bound);
            var check = exclusive
                        ? (Expression v) => v.greaterOrEqualTo(literal)
                        : (Expression v) => v.greaterThan(literal);
            var requiredString = _Rule((v) => check(v),
                                       _outOfBound,
                                       validatorFactory.violationList);
            rules.add(requiredString);
        }
        return rules;
    }

    Expression _outOfBound(Expression value) {
        var param = 'v';
        var floatValue = refer('FloatValue', protoWrappersImport)
            .newInstance([])
            .property('copyWith')
            .call([Method((b) => b
            ..requiredParameters.add(Parameter((b) => b.name = param))
            ..body = refer(param)
                .property('value')
                .assign(value)
                .statement).closure]);
        var any = refer('Any', protoAnyImport).property('pack').call([floatValue]);
        return violationRef.call([literalString('Float field is out of bound.'),
                                  literalString(validatorFactory.fullName),
                                  literalList([field.name]),
                                  any]);
    }
}
