import 'package:dart_code_gen/spine/base/field_path.pb.dart' as _i4;
import 'package:dart_code_gen/spine/net/url.pb.dart';
import 'package:dart_code_gen/spine/validate/validation_error.pb.dart' as _i1;
import 'package:protobuf/protobuf.dart' as _i2;

// TODO:2019-10-10:dmytro.dashenkov: Sample of generated code. Delete after debugging.

final Map<String, _i1.ValidationError Function(_i2.GeneratedMessage)>
validators = <String, _i1.ValidationError Function(_i2.GeneratedMessage)>{
    'google.protobuf.Any': (msg) {
        return null;
    },
    'google.protobuf.SourceContext': (msg) {
        return null;
    },
    'google.protobuf.Type': (msg) {
        return null;
    },
    'google.protobuf.Field': (msg) {
        return null;
    },
    'google.protobuf.Enum': (msg) {
        return null;
    },
    'google.protobuf.EnumValue': (msg) {
        return null;
    },
    'google.protobuf.Option': (msg) {
        return null;
    },
    'google.protobuf.Api': (msg) {
        return null;
    },
    'google.protobuf.Method': (msg) {
        return null;
    },
    'google.protobuf.Mixin': (msg) {
        return null;
    },
    'google.protobuf.FileDescriptorSet': (msg) {
        return null;
    },
    'google.protobuf.FileDescriptorProto': (msg) {
        return null;
    },
    'google.protobuf.DescriptorProto': (msg) {
        return null;
    },
    'google.protobuf.ExtensionRangeOptions': (msg) {
        return null;
    },
    'google.protobuf.FieldDescriptorProto': (msg) {
        return null;
    },
    'google.protobuf.OneofDescriptorProto': (msg) {
        return null;
    },
    'google.protobuf.EnumDescriptorProto': (msg) {
        return null;
    },
    'google.protobuf.EnumValueDescriptorProto': (msg) {
        return null;
    },
    'google.protobuf.ServiceDescriptorProto': (msg) {
        return null;
    },
    'google.protobuf.MethodDescriptorProto': (msg) {
        return null;
    },
    'google.protobuf.FileOptions': (msg) {
        return null;
    },
    'google.protobuf.MessageOptions': (msg) {
        return null;
    },
    'google.protobuf.FieldOptions': (msg) {
        return null;
    },
    'google.protobuf.OneofOptions': (msg) {
        return null;
    },
    'google.protobuf.EnumOptions': (msg) {
        return null;
    },
    'google.protobuf.EnumValueOptions': (msg) {
        return null;
    },
    'google.protobuf.ServiceOptions': (msg) {
        return null;
    },
    'google.protobuf.MethodOptions': (msg) {
        return null;
    },
    'google.protobuf.UninterpretedOption': (msg) {
        return null;
    },
    'google.protobuf.SourceCodeInfo': (msg) {
        return null;
    },
    'google.protobuf.GeneratedCodeInfo': (msg) {
        return null;
    },
    'google.protobuf.compiler.Version': (msg) {
        return null;
    },
    'google.protobuf.compiler.CodeGeneratorRequest': (msg) {
        return null;
    },
    'google.protobuf.compiler.CodeGeneratorResponse': (msg) {
        return null;
    },
    'google.protobuf.Duration': (msg) {
        return null;
    },
    'google.protobuf.Empty': (msg) {
        return null;
    },
    'google.protobuf.FieldMask': (msg) {
        return null;
    },
    'google.protobuf.Struct': (msg) {
        return null;
    },
    'google.protobuf.Value': (msg) {
        return null;
    },
    'google.protobuf.ListValue': (msg) {
        return null;
    },
    'google.protobuf.Timestamp': (msg) {
        return null;
    },
    'google.protobuf.DoubleValue': (msg) {
        return null;
    },
    'google.protobuf.FloatValue': (msg) {
        return null;
    },
    'google.protobuf.Int64Value': (msg) {
        return null;
    },
    'google.protobuf.UInt64Value': (msg) {
        return null;
    },
    'google.protobuf.Int32Value': (msg) {
        return null;
    },
    'google.protobuf.UInt32Value': (msg) {
        return null;
    },
    'google.protobuf.BoolValue': (msg) {
        return null;
    },
    'google.protobuf.StringValue': (msg) {
        return null;
    },
    'google.protobuf.BytesValue': (msg) {
        return null;
    },
    '.IfMissingOption': (msg) {
        return null;
    },
    '.MinOption': (msg) {
        return null;
    },
    '.MaxOption': (msg) {
        return null;
    },
    '.PatternOption': (msg) {
        return null;
    },
    '.IfInvalidOption': (msg) {
        return null;
    },
    '.GoesOption': (msg) {
        return null;
    },
    '.EntityOption': (msg) {
        return null;
    },
    '.IsOption': (msg) {
        return null;
    },
    'spine.base.FieldPath': (msg) {
        return null;
    },
    'spine.validate.ValidationError': (msg) {
        return null;
    },
    'spine.validate.ConstraintViolation': (msg) {
        return null;
    },
    'spine.base.Error': (msg) {
        return null;
    },
    'spine.base.FieldFilter': (msg) {
        return null;
    },
    'spine.net.EmailAddress': (msg) {
        return null;
    },
    'spine.net.InternetDomain': (msg) {
        return null;
    },
    'spine.net.Url': (msg) {
        var violations = <_i1.ConstraintViolation>[];
        (msg as Url).spec.isEmpty
        ? violations
            .add(_violation('Field must be set.', 'spine.net.Url', ['spec']))
        : null;
        var error = _i1.ValidationError();
        error.constraintViolation.addAll(violations);
        return error;
    },
    'spine.net.Uri': (msg) {
        return null;
    },
    'spine.people.PersonName': (msg) {
        return null;
    },
    'spine.ui.Color': (msg) {
        return null;
    }
};
_i1.ConstraintViolation _violation(
    String msgFormat, String typeName, List<String> fieldPath) {
    var violation = _i1.ConstraintViolation();
    violation.msgFormat = msgFormat;
    violation.typeName = typeName;
    var path = _i4.FieldPath();
    path.fieldName.addAll(fieldPath);
    violation.fieldPath = path;
    return violation;
}
