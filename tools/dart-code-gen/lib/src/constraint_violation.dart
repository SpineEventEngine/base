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

import 'imports.dart';

const _violation = '_violation';
const violationRef = Reference(_violation);

createViolationFactory(String standardPackage) {
    return Method((b) {
        var result = 'violation';
        var resultRef = refer(result);

        var msgFormat = 'msgFormat';
        var typeName = 'typeName';
        var fieldPath = 'fieldPath';
        var actualValue = 'actualValue';

        b..name = _violation
         ..requiredParameters.add(Parameter((b) => b..type = refer('String')..name = msgFormat))
         ..requiredParameters.add(Parameter((b) => b..type = refer('String')..name = typeName))
         ..requiredParameters.add(Parameter((b) => b..type = TypeReference((b) => b..symbol = 'List'..types.add(refer('String')))..name = fieldPath))
         ..optionalParameters.add(Parameter((b) => b..type = refer('Any', protoAnyImport(standardPackage))..name = actualValue))
         ..returns = refer('ConstraintViolation', validationErrorImport(standardPackage))
         ..body = Block.of([
                refer('ConstraintViolation', validationErrorImport(standardPackage)).newInstance([]).assignVar(result).statement,
                resultRef.property('msgFormat').assign(refer(msgFormat)).statement,
                resultRef.property('typeName').assign(refer(typeName)).statement,
                refer('FieldPath', 'package:$standardPackage/spine/base/field_path.pb.dart').newInstance([]).assignVar('path').statement,
                refer('path').property('fieldName').property('addAll').call([refer(fieldPath)]).statement,
                resultRef.property('fieldPath').assign(refer('path')).statement,
                resultRef.property('fieldValue').assign(refer(actualValue)).statement,
                resultRef.returned.statement
            ]);
    });
}

