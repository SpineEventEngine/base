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
import 'package:dart_code_gen/src/bytes_validator_factory.dart';

import 'constraint_violation.dart';
import 'enum_validator_factory.dart';
import 'message_validator_factory.dart';
import 'number_validator_factory.dart';
import 'string_validator_factory.dart';
import 'validator_factory.dart';

/// Factory of validation code for a given message field.
///
class FieldValidatorFactory {

    /// The [ValidatorFactory] for the declaring message type.
    final ValidatorFactory validatorFactory;

    /// The field to validate.
    final FieldDescriptorProto field;

    FieldValidatorFactory(this.validatorFactory, this.field);

    /// Creates a new `FieldValidatorFactory` for the given field.
    ///
    /// May return `null` to signify that no validation is required for the given field.
    ///
    factory FieldValidatorFactory.forField(FieldDescriptorProto field, ValidatorFactory factory) {
        var scalarFactory = ScalarFieldValidatorFactory._forType(field, factory);
        var repeated = field.label == FieldDescriptorProto_Label.LABEL_REPEATED;
        if (repeated) {
            return RepeatedFieldValidatorFactory(factory, field, scalarFactory);
        } else {
            return scalarFactory;
        }
    }

    /// Generates validator code for the specified field.
    ///
    /// The validator obtains the field value via the given [fieldValue] expression.
    ///
    /// If any constrains violations are discovered, they are added to
    /// the [ValidatorFactory.violationList] of the [validatorFactory].
    ///
    Code createFieldValidator(Expression fieldValue) => null;

    /// Checks if the validated field is required.
    ///
    /// Returns `true` if the field is required and `false` if it is optional.
    ///
    bool isRequired() {
        var options = field.options;
        return options.hasExtension(Options.required)
            && options.getExtension(Options.required);
    }

    /// Creates a new validation rule with the given parameters.
    Rule newRule(LazyCondition condition, LazyViolation violation) {
        return Rule._(condition, violation, validatorFactory.violationList);
    }

    /// Creates a validation for the `(required)` constraint.
    ///
    /// The [condition] determines whether or not the field value if set. The conditional expression
    /// should evaluate into `true` if the field is **NOT** set.
    ///
    Rule createRequiredRule(LazyCondition condition) {
        return newRule(condition, (v) => _requiredMissing());
    }

    /// Generates an expression which constructs a `ConstraintViolation` for a missing required
    /// field.
    Expression _requiredMissing() {
        return violationRef.call([literalString('Field must be set.'),
                                  literalString(validatorFactory.fullTypeName),
                                  literalList([field.name])]);
    }
}

class ScalarFieldValidatorFactory extends FieldValidatorFactory {

    ScalarFieldValidatorFactory(ValidatorFactory validatorFactory, FieldDescriptorProto field)
        : super(validatorFactory, field);

    factory ScalarFieldValidatorFactory._forType(FieldDescriptorProto field,
                                                 ValidatorFactory factory) {
        var type = field.type;
        switch (type) {
            case FieldDescriptorProto_Type.TYPE_STRING:
                return StringValidatorFactory(factory, field);
            case FieldDescriptorProto_Type.TYPE_DOUBLE:
                return DoubleValidatorFactory.forDouble(factory, field);
            case FieldDescriptorProto_Type.TYPE_FLOAT:
                return DoubleValidatorFactory.forFloat(factory, field);
            case FieldDescriptorProto_Type.TYPE_INT32:
            case FieldDescriptorProto_Type.TYPE_SINT32:
            case FieldDescriptorProto_Type.TYPE_FIXED32:
            case FieldDescriptorProto_Type.TYPE_SFIXED32:
                return IntValidatorFactory.forInt32(factory, field);
            case FieldDescriptorProto_Type.TYPE_INT64:
            case FieldDescriptorProto_Type.TYPE_SINT64:
            case FieldDescriptorProto_Type.TYPE_FIXED64:
            case FieldDescriptorProto_Type.TYPE_SFIXED64:
                return IntValidatorFactory.forInt64(factory, field);
            case FieldDescriptorProto_Type.TYPE_UINT32:
                return IntValidatorFactory.forUInt32(factory, field);
            case FieldDescriptorProto_Type.TYPE_UINT64:
                return IntValidatorFactory.forUInt64(factory, field);
            case FieldDescriptorProto_Type.TYPE_BYTES:
                return BytesValidatorFactory(factory, field);
            case FieldDescriptorProto_Type.TYPE_ENUM:
                return EnumValidatorFactory(factory, field);
            case FieldDescriptorProto_Type.TYPE_MESSAGE:
                return MessageValidatorFactory(factory, field);
        }
        return null;
    }

    /// Generates validator code for the specified field.
    ///
    /// The validator obtains the field value via the given [fieldValue] expression.
    ///
    /// If any constrains violations are discovered, they are added to
    /// the [ValidatorFactory.violationList] of the [validatorFactory].
    ///
    @override
    Code createFieldValidator(Expression fieldValue) {
        var statements = rules()
            .map((r) => r._eval(fieldValue))
            .map((expression) => expression.statement);
        return statements.isNotEmpty
               ? Block.of(statements)
               : null;
    }

    /// Obtains validation rules to apply to the field..
    Iterable<Rule> rules() => null;
}

class RepeatedFieldValidatorFactory extends FieldValidatorFactory {

    final FieldValidatorFactory _scalar;

    RepeatedFieldValidatorFactory(ValidatorFactory validatorFactory,
                                  FieldDescriptorProto field,
                                  this._scalar)
        : super(validatorFactory, field);

    @override
    Code createFieldValidator(Expression field) {
        var validation = <Expression>[];
        if (isRequired()) {
            var requiredRule = createRequiredRule((v) => v.property('isEmpty'));
            validation.add(requiredRule._eval(field));
        }
        var values = 'values_${this.field.name}';
        var valuesRef = refer(values);
        var valueList = field.isA(refer('Map'))
                             .conditional(field.asA(refer('dynamic')).property('values'), field)
                             .assignVar(values);
        validation.add(valueList);
        var element = 'element';
        var elementRef = refer(element);
        var elementValidation = _scalar.createFieldValidator(elementRef);
        if (elementValidation != null) {
            var nonNullCheck = refer('ArgumentError')
                .property('checkNotNull')
                .call([elementRef]);
            var validatingLambda = Method.returnsVoid((b) {
                b..requiredParameters.add(Parameter((b) => b..name = element))
                    ..body = Block.of([nonNullCheck.statement, elementValidation])
                    ..lambda = false;
            });
            var validateEachElement = valuesRef.property('forEach')
                .call([validatingLambda.closure]);
            validation.add(validateEachElement);
        }
        return Block.of(validation.map((expression) => expression.statement));
    }
}


/// A validation rule.
///
/// Represents an atomic constraint which should be followed by a message field.
///
/// Typically, one rule represents one validation option for a given field type.
///
class Rule {

    final LazyCondition _condition;
    final LazyViolation _violation;
    final Expression _targetViolationList;

    /// Creates a new rule.
    ///
    /// The [_condition] expression is evaluated to find out if the rule is broken or not.
    ///
    /// The [_violation] expression is evaluated to obtain a `ConstraintViolation` if the rule is
    /// broken.
    ///
    /// The [_targetViolationList] is an expression which represents a list which accumulates all
    /// the `ConstraintViolation`s. If the rule is broken, the violation generated by [_violation]
    /// is added to the list.
    ///
    Rule._(this._condition, this._violation, this._targetViolationList);

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
    Expression _eval(Expression fieldValue) {
        var ternaryOperator = _condition(fieldValue).conditional(
            _targetViolationList
                .property('add')
                .call([_violation(fieldValue)]),
            literalNull);
        return ternaryOperator;
    }
}

/// A function of a field value expresion to a `ConstraintViolation` expression.
typedef Expression LazyViolation(Expression fieldValue);

/// A function of a field value expresion to a boolean expression representing a constraint.
///
/// The resulting expression should return a `bool`:
///  - `true` if the constraint is violated;
///  - `false` if the constraint obeyed.
///
typedef Expression LazyCondition(Expression fieldValue);
