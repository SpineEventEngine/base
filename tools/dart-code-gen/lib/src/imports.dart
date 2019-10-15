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

/// The Protobuf runtime library.
const protobufImport = 'package:protobuf/protobuf.dart';

/// `spine/validate/validation_error.pb.dart`.
String validationErrorImport(String standardPackage) =>
    'package:$standardPackage/spine/validate/validation_error.pb.dart';

/// `google/protobuf/wrappers.pb.dart`.
String protoWrappersImport(String standardPackage) =>
    'package:$standardPackage/google/protobuf/wrappers.pb.dart';

/// `google/protobuf/any.pb.dart`.
String protoAnyImport(String standardPackage) =>
    'package:$standardPackage/google/protobuf/any.pb.dart';

/// `spine/base/field_path.pb.dart`.
String fieldPathImport(String standardPackage) =>
    'package:$standardPackage/spine/base/field_path.pb.dart';
