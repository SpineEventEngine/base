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
import 'package:dart_code_gen/src/field_validator_factory.dart';

import 'validator_factory.dart';

class FieldTupleValidatorFactory {

    final ValidatorFactory _validator;
    final List<_Combination> _combinations;

    FieldTupleValidatorFactory._(this._validator, this._combinations);

    factory FieldTupleValidatorFactory.forTuple(String tuple, ValidatorFactory validator) {
        var combinations = tuple
            .split('|')
            .map((c) => c.split('&'))
            .map((fields) => _Combination.ofFields(fields, validator))
            .toList();
        return FieldTupleValidatorFactory._(validator, combinations);
    }
    
    Code generate(Expression message) => null;
}

class _Combination {

    final List<String> fieldNames;
    final ValidatorFactory _validator;

    _Combination.ofFields(Iterable<String> fieldNames, this._validator)
        : this.fieldNames = List.of(fieldNames)
                                .map((n) => n.trim());

    FieldValidatorFactory _validatorFactory(String fieldName) {
        var field = _field(fieldName);
        var factory = FieldValidatorFactory.forField(field, _validator);
        if (!factory.supportsRequired()) {
            throw StateError('Field `${_validator.type.name}.${field}` cannot be required.');
        }
        return factory;
    }

    FieldDescriptorProto _field(String name) {
        var type = _validator.type;
        FieldDescriptorProto field = type
                .field
                .where((t) => t.name == name)
                .first;
        ArgumentError.checkNotNull(field, '`${type.name}` does not declare field `${name}`.');
        return field;
    }
}
