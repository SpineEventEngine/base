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
import 'package:dart_style/dart_style.dart';

String generateClassTest(FileDescriptorSet descriptorSet) {
    var code = Library((b) =>
        b..body.add(_createValidatorMap(descriptorSet))
         ..body.add(_createViolationFactory())
    );
    var emitter = DartEmitter(Allocator.simplePrefixing());
    var formatter = DartFormatter();
    return formatter.format(code.accept(emitter).toString());
}

const _msg = 'msg';

Field _createValidatorMap(FileDescriptorSet descriptorSet) {
    var keyType = refer('String');
    var validationError = refer('ValidationError', 'package:spine_base/spine/validate/validation_error.pb.dart');
    var valueType = FunctionType((b) => b
        ..requiredParameters.add(refer('GeneratedMessage', 'package:protobuf/protobuf.dart'))
        ..returnType = validationError);
    var validatorMap = Map<String, Expression>();
    for (var file in descriptorSet.file) {
        for (var type in file.messageType) {
            var name = '${file.package}.${type.name}';
            var lambda = Method((b) => b
                ..body = _createValidator(type, name)
                ..requiredParameters.add(Parameter((b) => b.name = _msg)))
                .closure;
            validatorMap[name] = lambda;
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

const _violations = 'violations';

Code _createValidator(DescriptorProto descriptor, String fullName) {
    var violation = refer('ConstraintViolation', 'package:spine_base/spine/validate/validation_error.pb.dart');
    var validations = <Code>[];
    for (var field in descriptor.field) {
        var validator = _createFieldValidator(field, fullName, descriptor.name);
        if (validator != null) {
            validations.add(validator);
        }
    }
    if (validations.isEmpty) {
        return literalNull.returned.statement;
    } else {
        var statements = <Code>[];
        statements.add(literalList([], violation).assignVar(_violations).statement);
        statements.addAll(validations);
        statements.add(refer('ValidationError', 'package:spine_base/spine/validate/validation_error.proto').newInstance([]).assignVar('error').statement);
        statements.add(refer('error').property('constraintViolation').property('addAll').call([refer(_violations)]).statement);
        statements.add(refer('error').returned.statement);
        return Block.of(statements);
    }
}

Code _createFieldValidator(FieldDescriptorProto field, String fullTypeName, String shortTypeName) {
    if (field.options.getExtension(Options.required)) {
        var fieldValue = refer(_msg).asA(refer(shortTypeName)).property(field.name);
        if (field.type == FieldDescriptorProto_Type.TYPE_STRING) {
            return Block.of([
                fieldValue.property('isEmpty').conditional(
                    refer(_violations).property('add').call([_violationRef.call([literalString('Field must be set.'), literalString(fullTypeName), literalList([field.name])])]),
                    literalNull)
                .statement
            ]);
        } else {
            return null;
        }
    } else {
        return null;
    }
}

const _violation = '_violation';
const _violationRef = Reference(_violation);

_createViolationFactory() {
    return Method((b) {
        var violation = 'violation';
        var violationRef = refer(violation);

        var msgFormat = 'msgFormat';
        var typeName = 'typeName';
        var fieldPath = 'fieldPath';

        b..name = '_violation'
         ..returns = refer('ConstraintViolation', 'package:spine_base/spine/validate/validation_error.pb.dart')
         ..requiredParameters.add(Parameter((b) => b..type = refer('String')..name = msgFormat))
         ..requiredParameters.add(Parameter((b) => b..type = refer('String')..name = typeName))
         ..requiredParameters.add(Parameter((b) => b..type = TypeReference((b) => b..symbol = 'List'..types.add(refer('String')))..name = fieldPath))

         ..body = Block.of([
             refer('ConstraintViolation', 'package:spine_base/spine/validate/validation_error.pb.dart').newInstance([]).assignVar(violation).statement,
             violationRef.property('msgFormat').assign(refer(msgFormat)).statement,
             violationRef.property('typeName').assign(refer(typeName)).statement,
             refer('FieldPath', 'package:spine_base/spine/base/field_path.pb.dart').newInstance([]).assignVar('path').statement,
             refer('path').property('fieldName').property('addAll').call([refer(fieldPath)]).statement,
             violationRef.property('fieldPath').assign(refer('path')).statement,
             violationRef.returned.statement
           ]);
    });
}
