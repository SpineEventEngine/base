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
import 'package:dart_code_gen/src/constraint_violation.dart';
import 'package:dart_code_gen/src/imports.dart';
import 'package:dart_code_gen/src/validator_factory.dart';
import 'package:dart_style/dart_style.dart';

String generateClassTest(FileDescriptorSet descriptorSet) {
    var allocator = Allocator.simplePrefixing();
    var code = Library((b) =>
        b..body.add(_createValidatorMap(descriptorSet, allocator))
         ..body.add(createViolationFactory())
    );
    var emitter = DartEmitter(allocator);
    var formatter = DartFormatter();
    return formatter.format(code.accept(emitter).toString());
}

Field _createValidatorMap(FileDescriptorSet descriptorSet, Allocator allocator) {
    var keyType = refer('String');
    var validationError = refer('ValidationError', validationErrorImport);
    var valueType = FunctionType((b) => b
        ..requiredParameters.add(refer('GeneratedMessage', protobufImport))
        ..returnType = validationError);
    var validatorMap = Map<String, Expression>();
    for (var file in descriptorSet.file) {
        for (var type in file.messageType) {
            var factory = ValidatorFactory(file, type, allocator);
            validatorMap[factory.fullName] = factory.createValidator();
        }
    }
    return Field((b) => b
        ..name = 'validators'
        ..modifier = FieldModifier.final$
        ..type = TypeReference((b) => b
            ..symbol = 'Map'
            ..types.addAll([keyType, valueType]))
        ..assignment = literalMap(validatorMap, keyType, valueType).code
    );
}
