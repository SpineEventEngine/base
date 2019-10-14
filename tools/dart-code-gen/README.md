# Dart Validation Code Generator

A command line tool which generates validation code for protobuf code basing on the validation
options from `spine/options.proto`.

## Usage 

The generator accepts several command line arguments:
 - Required option `--descriptor` specifies the path to a file which contains
   a `google.protobuf.FileDescriptorSet`. The descriptor set contains all the Protobuf types
   for which the tool must generate validation code.
 - Required option `--destination` specifies the path to the file into which the validation code
   must be written. If the file does not exist, it is created.
 - Option `--standard-types` specifies a Dart package which contains standard Protobuf types. 
   The default value of this option is `spine_client`. The package should contain types:
     - declared in the `google.protobuf.*` package;
     - declared in the base module in Spine.
 - Option `--import-prefix` specifies the path prefix for `.pb.dart` files generated for types
   listed in the `descriptor`. Defaults to an empty path, implying that the generated files are
   located in the same directory as the `destination`.
 - Flag `--stdout` makes the tool print the output Dart code into the standard output stream, as
   well as writing it into the `destination` file.
 - Flag `--no-stdout` ensures that the tool does not print Dart code into the standard output 
   stream. This is the default behaviour.
 - Flag `--help` (or `-h` for short), makes the tool print a brief usage guide. When called with
   this flag, all the other flags are ignored.
