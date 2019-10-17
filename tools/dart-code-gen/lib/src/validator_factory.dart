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

import '../dart_code_gen.dart';
import '../google/protobuf/descriptor.pb.dart';
import 'constraint_violation.dart';
import 'field_validator_factory.dart';
import 'imports.dart';

const _violations = 'violations';
const _msg = 'msg';

/// Factory of message validation code for a Protobuf type.
///
/// For a given Protobuf message type, generates a lambda which accepts a single message and creates
/// a `spine.validate.ValidationError`. If the given message is valid, the lambda returns `null`.
/// Otherwise, it returns all `ConstraintViolation`s for the message as a single error.
///
/// Note that the type of the input message is not checked. Users should wrap calls to validators
/// in type-aware abstractions instead of referring to them directly.
///
class ValidatorFactory {

    final FileDescriptorProto file;
    final DescriptorProto type;
    final Allocator allocator;
    final Properties properties;

    ValidatorFactory(this.file, this.type, this.allocator, this.properties);

    String get fullTypeName => '${file.package}.${type.name}';
    
    Reference get violationList => refer(_violations);
    
    String get _fileName {
        var endIndex = file.name.length - '.proto'.length;
        var filePathWithoutExtension = file.name.substring(0, endIndex);
        return filePathWithoutExtension + '.pb.dart';
    }
    
    Expression get _emptyValidationError =>
        refer('ValidationError', validationErrorImport(properties.standardPackage))
            .newInstance([]);

    /// Creates a validator expression.
    ///
    Expression createValidator() {
        var param = Parameter((b) => b
            ..type = refer('GeneratedMessage', protobufImport)
            ..name = _msg);
        try {
            return Method((b) => b
                ..requiredParameters.add(param)
                ..body = _createValidator()).closure;
        } catch (e) {
            throw StateError('Cannot generate validation code for `$fullTypeName`. '
                             '${e.toString()}');
        }
    }

    Code _createValidator() {
        var validations = <Code>[];
        for (var field in type.field) {
            var validator = _createFieldValidator(field);
            if (validator != null) {
                validations.add(validator);
            }
        }
        if (validations.isEmpty) {
            return _emptyValidationError.returned.statement;
        } else {
            return _collectFieldChecks(validations);
        }
    }

    Block _collectFieldChecks(List<Code> validations) {
        var statements = <Code>[];
        statements.add(_newViolationList().statement);
        statements.addAll(validations);
        var error = 'error';
        var errorRef = refer(error);
        statements.add(_newValidationError(error).statement);
        statements.add(_fillInViolations(errorRef).statement);
        statements.add(errorRef.returned.statement);
        return Block.of(statements);
    }

    Expression _fillInViolations(Reference errorRef) {
        return errorRef.property('constraintViolation')
            .property('addAll')
            .call([refer(_violations)]);
    }

    Expression _newViolationList() {
        return literalList([], violationTypeRef(properties.standardPackage))
            .assignVar(_violations);
    }

    Expression _newValidationError(String error) {
        return _emptyValidationError.assignVar(error);
    }

    /// Generates validation code for a given field.
    ///
    /// For this, the message is cast to the expected type and the field value is obtained.
    ///
    /// See [FieldValidatorFactory] for more on field validation.
    ///
    Code _createFieldValidator(FieldDescriptorProto field) {
        var prefix = properties.importPrefix;
        var importUri = prefix.isNotEmpty
                        ? '$prefix/$_fileName'
                        : _fileName;
        var validatedMessageType = refer(type.name, importUri);
        var factory = FieldValidatorFactory.forField(field, this);
        if (factory != null) {
            var fieldValue = refer(_msg).asA(validatedMessageType).property(_fieldName(field));
            return factory.createFieldValidator(fieldValue);
        } else {
            return null;
        }
    }

    /// Obtains a Dart name of the given Protobuf field.
    ///
    /// Dart Protobuf plugin also supports custom names generated from the custom `(dart_name)`
    /// option. However, since the Protobuf sources for that option are not published, there is
    /// no easy way to use this option. Thus, Spine code generation does not support it.
    ///
    String _fieldName(FieldDescriptorProto field) {
        var protoName = field.name;
        var words = protoName.split('_');
        var first = words[0];
        var capitalized = List.of(words.map(_capitalize));
        capitalized[0] = first;
        return capitalized.join('');
    }

    String _capitalize(String word) {
        return word.isEmpty
               ? word
               : '${word[0].toUpperCase()}${word.substring(1)}';
    }
}
