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

import 'field_validator_factory.dart';
import 'imports.dart';

const _violations = 'violations';
const _msg = 'msg';

class ValidatorFactory {

    final FileDescriptorProto file;
    final DescriptorProto type;
    final Allocator allocator;

    ValidatorFactory(this.file, this.type, this.allocator);

    String get fullName => '${file.package}.${type.name}';
    String get fileName => file.name.substring(0, file.name.length - 'proto'.length) + 'pb.dart';
    Reference get violationList => refer(_violations);

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
        statements.add(newViolationList().statement);
        statements.addAll(validations);
        var error = 'error';
        var errorRef = refer(error);
        statements.add(newValidationError(error).statement);
        statements.add(fillInViolations(errorRef).statement);
        statements.add(errorRef.returned.statement);
      return Block.of(statements);
    }

    Expression fillInViolations(Reference errorRef) {
        return errorRef.property('constraintViolation')
            .property('addAll')
            .call([refer(_violations)]);
    }

    Expression newViolationList() {
        return literalList([], refer('ConstraintViolation', validationErrorImport))
            .assignVar(_violations);
    }

    Expression newValidationError(String error) {
        return refer('ValidationError', validationErrorImport)
            .newInstance([])
            .assignVar(error);
    }

    Code _createFieldValidator(FieldDescriptorProto field) {
        var validatedMessageType = refer(type.name, fileName);
        var factory = forField(field, this);
        if (factory != null) {
            var fieldValue = refer(_msg).asA(validatedMessageType).property(field.name);
            return factory.createFieldValidator(fieldValue);
        } else {
            return null;
        }
    }
}
