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

const _violationType = 'ConstraintViolation';
const _violation = '_violation';

/// The reference to the method which constructs a `spine.validate.ConstraintViolation` from given
/// parameters.
///
/// To produce an expression which invokes this method, call `call(..)` and pass method arguments:
///  1. The error message string.
///  2. The name of the validated type.
///  3. A list of strings representing a path to the field as defined in `spine.base.FieldPath`.
///  4. Optionally, a `google.protobuf.Any` with the actual value of the field.
///
const violationRef = Reference(_violation);

Reference violationTypeRef(String standardPackage) =>
    Reference(_violationType, validationErrorImport(standardPackage));

/// Produces a method which constructs a `ConstraintViolation` with given parameters.
///
/// The method requires an import of `spine.validate.ConstraintViolation` from
/// the [standardPackage].
///
/// To call the method, use [violationTypeRef].
///
createViolationFactory(String standardPackage) {
    return Method((b) {
        var result = 'violation';
        var resultRef = refer(result);

        var msgFormat = 'msgFormat';
        var typeName = 'typeName';
        var fieldPath = 'fieldPath';
        var actualValue = 'actualValue';
        var actualValueRef = refer(actualValue);
        
        var listOfStrings = TypeReference((b) => b
            ..symbol = 'List'
            ..types.add(refer('String')));
        var fieldPathParam = Parameter((b) => b
            ..type = listOfStrings
            ..name = fieldPath);
        var anyType = refer('Any', protoAnyImport(standardPackage));
        var actualValueParam = Parameter((b) => b
            ..type = anyType
            ..name = actualValue);
        b.name = _violation;
        b.requiredParameters
            ..add(Parameter((b) => b..type = refer('String')..name = msgFormat))
            ..add(Parameter((b) => b..type = refer('String')..name = typeName))
            ..add(fieldPathParam);
        b.optionalParameters.add(actualValueParam);
        var path = 'path';
        var type = violationTypeRef(standardPackage);
        b..returns = type
         ..body = Block.of(<Expression>[
             actualValueRef.assign(actualValueRef.notEqualTo(literalNull).conditional(
                                       actualValueRef,
                                       anyType.newInstance([]))),
             type.newInstance([]).assignVar(result),
             resultRef.property('msgFormat').assign(refer(msgFormat)),
             resultRef.property('typeName').assign(refer(typeName)),
             refer('FieldPath', fieldPathImport(standardPackage)).newInstance([]).assignVar(path),
             refer(path).property('fieldName').property('addAll').call([refer(fieldPath)]),
             resultRef.property('fieldPath').assign(refer(path)),
             resultRef.property('fieldValue').assign(actualValueRef),
             resultRef.returned
         ].map((expression) => expression.statement));
    });
}
