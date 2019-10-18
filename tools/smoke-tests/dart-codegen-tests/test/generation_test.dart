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

import 'package:codegen_test/spine/test/tools/dart/test.pb.dart';
import 'package:codegen_test/types.dart' as generatedTypes;
import 'package:protobuf/protobuf.dart';
import 'package:test/test.dart';

const dartSmokeTestType = 'type.spine.io/spine.test.tools.DartSmokeTest';
const nestedType = 'type.spine.io/spine.test.tools.First.Second.Third';

void main() {
    group('Known types generation should', () {

        var types;

        setUp(() {
           types = generatedTypes.types();
        });

        test('generate file', () {
           expect(types.typeUrlToInfo, isNotNull);
           expect(types.defaultToTypeUrl, isNotNull);
        });

        test('generate type URL to BuilderInfo mapping for locally defined types', () {
            var info = types.typeUrlToInfo[dartSmokeTestType];
            expect(info, isA<BuilderInfo>());
            expect(info.qualifiedMessageName, equals('spine.test.tools.DartSmokeTest'));
        });

        test('generate default instance to type URL mapping for locally defined types', () {
            var type = types.defaultToTypeUrl[DartSmokeTest.getDefault()];
            expect(type, equals(dartSmokeTestType));
        });

        test('generate type URL to BuilderInfo mapping for nested types', () {
            var info = types.typeUrlToInfo[nestedType];
            expect(info, isA<BuilderInfo>());
            expect(info.qualifiedMessageName, equals('spine.test.tools.First.Second.Third'));
        });

        test('generate default instance to type URL mapping for dependencies', () {
            var type = types.defaultToTypeUrl[First_Second_Third.getDefault()];
            expect(type, equals(nestedType));
        });
    });
}
