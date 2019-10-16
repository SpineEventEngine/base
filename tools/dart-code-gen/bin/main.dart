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

import 'dart:io';

import 'package:args/args.dart';
import 'package:dart_code_gen/dart_code_gen.dart' as dart_code_gen;
import 'package:dart_code_gen/google/protobuf/descriptor.pb.dart';
import 'package:dart_code_gen/spine/options.pb.dart';
import 'package:protobuf/protobuf.dart';

const String descriptorArgument = 'descriptor';
const String destinationArgument = 'destination';
const String stdPackageArgument = 'standard-types';
const String importPrefixArgument = 'import-prefix';

const String stdoutFlag = 'stdout';
const String helpFlag = 'help';

/// Launches the Dart validation code generator.
///
main(List<String> arguments) {
    ArgParser parser = _createParser();
    var args = parser.parse(arguments);
    var help = args[helpFlag];
    if (help) {
        stdout.writeln('dart_code_gen — a command line application for generating Dart validation '
                       'code based on Spine validation options.');
        stdout.writeln(parser.usage);
    } else {
        _launch_code_gen(args);
    }
}

void _launch_code_gen(ArgResults args) {
    var descriptorPath = _getRequired(args, descriptorArgument);
    var destinationPath = _getRequired(args, destinationArgument);
    var stdPackage = args[stdPackageArgument];
    var importPrefix = args[importPrefixArgument];

    var shouldPrint = args[stdoutFlag];

    var descFile = File(descriptorPath);
    _checkExists(descFile);
    var destinationFile = File(destinationPath);
    _ensureExists(destinationFile);

    FileDescriptorSet descriptors = _parseDescriptors(descFile);
    var properties = dart_code_gen.Properties(descriptors, stdPackage, importPrefix);
    var dartCode = dart_code_gen.generateValidators(properties);
    destinationFile.writeAsStringSync(dartCode, flush: true);
    if (shouldPrint) {
        stdout.write(dartCode);
    }
}

dynamic _getRequired(ArgResults args, String name) {
    var result = args[name];
    if (result == null) {
        throw ArgumentError('Option `$name` is required. Run with `--help` for the option list.');
    } else {
        return result;
    }
}

void _ensureExists(File file) {
  if (!file.existsSync()) {
      file.createSync(recursive: true);
  }
}

void _checkExists(File file) {
    if (!file.existsSync()) {
        throw ArgumentError('Descriptor file `${file.path}` does not exist.');
    }
}

FileDescriptorSet _parseDescriptors(File descFile) {
    var bytes = descFile.readAsBytesSync();
    ExtensionRegistry registry = _optionExtensions();
    var descriptors = FileDescriptorSet.fromBuffer(bytes, registry);
    return descriptors;
}

ExtensionRegistry _optionExtensions() {
    var registry = ExtensionRegistry();
    Options.registerAllExtensions(registry);
    return registry;
}

ArgParser _createParser() {
    var parser = ArgParser();
    parser.addOption(descriptorArgument,
                     help: 'Path to the file descriptor set file. This argument is required.');
    parser.addOption(destinationArgument,
                     help: 'Path to the destination file. This argument is required.');
    parser.addOption(stdPackageArgument,
                     help: 'Dart package which contains the standard Google Protobuf types '
                           'and basic Spine types.',
                     defaultsTo: 'spine_client');
    parser.addOption(importPrefixArgument,
                     help: 'Path prefix for imports of types which are vaidated.',
                     defaultsTo: '');
    parser.addFlag(stdoutFlag,
                   defaultsTo: false,
                   negatable: true,
                   help: 'If set, the Dart code is also written into the standard output.');
    parser.addFlag(helpFlag,
                   abbr: 'h',
                   defaultsTo: false,
                   negatable: false,
                   hide: true);
    return parser;
}
