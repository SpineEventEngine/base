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
        var singularFactory = SingularFieldValidatorFactory._forType(field, factory);
        var repeated = field.label == FieldDescriptorProto_Label.LABEL_REPEATED;
        if (repeated) {
            return RepeatedFieldValidatorFactory(factory, field, singularFactory);
        } else {
            return singularFactory;
        }
    }

    /// Generates validator code for the specified field.
    ///
    /// The validator obtains the field value via the given [fieldValue] expression.
    ///
    /// If any constrains violations are discovered, they are registered with
    /// the [validatorFactory].
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

    /// Determines if this field type supports `(required)` and related constrains.
    ///
    /// `true` by default.
    ///
    bool supportsRequired() => true;

    /// Creates a new validation rule with the given parameters.
    Rule newRule(LazyCondition condition, LazyViolation violation) {
        return Rule._(condition, violation, validatorFactory.report);
    }

    /// Creates a validation for the `(required)` constraint.
    ///
    Rule createRequiredRule() {
        return newRule(notSetCondition(), (v) => _requiredMissing());
    }

    /// Provides the condition under which a field value is considered not set.
    ///
    /// Is the field is `(required)`, it has to be set.
    ///
    LazyCondition notSetCondition() => null;

    /// Generates an expression which constructs a `ConstraintViolation` for a missing required
    /// field.
    Expression _requiredMissing() {
        return violationRef.call([literalString('Field must be set.'),
                                  literalString(validatorFactory.fullTypeName),
                                  literalList([field.name])]);
    }
}

/// A [FieldValidatorFactory] for non-`repeated` fields.
///
class SingularFieldValidatorFactory extends FieldValidatorFactory {

    SingularFieldValidatorFactory(ValidatorFactory validatorFactory, FieldDescriptorProto field)
        : super(validatorFactory, field);

    factory SingularFieldValidatorFactory._forType(FieldDescriptorProto field,
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

    /// Obtains validation rules to apply to the field.
    Iterable<Rule> rules() => null;
}

/// A [FieldValidatorFactory] for `repeated` and `map` fields.
///
class RepeatedFieldValidatorFactory extends FieldValidatorFactory {

    final FieldValidatorFactory _singular;

    RepeatedFieldValidatorFactory(ValidatorFactory validatorFactory,
                                  FieldDescriptorProto field,
                                  this._singular)
        : super(validatorFactory, field);

    @override
    Code createFieldValidator(Expression field) {
        var validation = <Expression>[];
        if (isRequired()) {
            var requiredRule = createRequiredRule();
            validation.add(requiredRule._eval(field));
        }
        var values = 'values_${this.field.name}';
        var valuesRef = refer(values);
        var validateDistinctList = _validateDistinct(valuesRef);
        var validateElements = _validateEachElement(valuesRef);
        if (validateElements != null || validateDistinctList != null) {
            var valueList = field.isA(refer('Map'))
                                 .conditional(field.asA(refer('dynamic')).property('values'), field)
                                 .assignVar(values);
            validation.add(valueList);
            if (validateDistinctList != null) {
                validation.add(validateDistinctList);
            }
            if (validateElements != null) {
                validation.add(validateElements);
            }
        }
        return Block.of(validation.map((expression) => expression.statement));
    }

    @override
    LazyCondition notSetCondition() => (v) => v.property('isEmpty');

    Expression _validateDistinct(Reference valuesRef) {
        var options = field.options;
        var option = Options.distinct;
        if (options.hasExtension(option) && options.getExtension(option)) {
            var length = 'length';
            LazyCondition condition = (v) =>
                v.property(length).notEqualTo(
                    v.property('toSet').call([]).property(length));
            LazyViolation violation = (v) =>
                violationRef.call([literalString('Collection must be distinct.'),
                                      literalString(validatorFactory.fullTypeName),
                                      literalList([field.name])]);
            var distinctRule = newRule(condition, violation);
            return distinctRule._eval(valuesRef);
        } else {
            return null;
        }
    }

    Expression _validateEachElement(Reference valuesRef) {
        var element = 'element';
        var elementRef = refer(element);
        var elementValidation = _singular.createFieldValidator(elementRef);
        if (elementValidation != null) {
            var nonNullCheck = refer('ArgumentError')
                .property('checkNotNull')
                .call([elementRef]);
            var validatingLambda = Method.returnsVoid((b) => b
                ..requiredParameters.add(Parameter((b) => b..name = element))
                ..body = Block.of([nonNullCheck.statement, elementValidation])
                ..lambda = false
            );
            var validateEachElement = valuesRef.property('forEach')
                                               .call([validatingLambda.closure]);
            return validateEachElement;
        } else {
            return null;
        }
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
    final ViolationConsumer _violationConsumer;

    /// Creates a new rule.
    ///
    /// The [_condition] expression is evaluated to find out if the rule is broken or not.
    ///
    /// The [_violation] expression is evaluated to obtain a `ConstraintViolation` if the rule is
    /// broken.
    ///
    /// The [_violationConsumer] function accepts an expression representing a single
    /// `ConstraintViolation` and produces an expression which registers the violation with
    /// a [ValidatorFactory].
    ///
    Rule._(this._condition, this._violation, this._violationConsumer);

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
            _violationConsumer(_violation(fieldValue)),
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
