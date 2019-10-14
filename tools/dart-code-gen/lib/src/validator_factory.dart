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

class ValidatorFactory {

    final FileDescriptorProto file;
    final DescriptorProto type;
    final Allocator allocator;
    final Properties properties;

    ValidatorFactory(this.file, this.type, this.allocator, this.properties);

    String get fullName => '${file.package}.${type.name}';
    Reference get violationList => refer(_violations);
    String get _fileName => file.name.substring(0, file.name.length - 'proto'.length) + 'pb.dart';

    Expression createValidator() {
        var param = Parameter((b) => b
            ..type = refer('GeneratedMessage', protobufImport)
            ..name = _msg);
        return Method((b) => b
            ..requiredParameters.add(param)
            ..body = _createValidator()).closure;
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
            return literalNull.returned.statement;
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
        return refer('ValidationError',
                     validationErrorImport(properties.standardPackage))
            .newInstance([])
            .assignVar(error);
    }

    Code _createFieldValidator(FieldDescriptorProto field) {
        var prefix = properties.importPrefix;
        var importUri = prefix.isNotEmpty
                        ? '$prefix/$_fileName'
                        : _fileName;
        var validatedMessageType = refer(type.name, importUri);
        var factory = forField(field, this);
        if (factory != null) {
            var fieldValue = refer(_msg).asA(validatedMessageType).property(field.name);
            return factory.createFieldValidator(fieldValue);
        } else {
            return null;
        }
    }
}
