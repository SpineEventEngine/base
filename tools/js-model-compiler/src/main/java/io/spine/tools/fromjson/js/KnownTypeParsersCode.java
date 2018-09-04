/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.fromjson.js;

// todo remake to resource
public class KnownTypeParsersCode {

    private static final String CODE =
            "\"use strict\";\n" +
                    "\n" +
                    "let known_types = require(\"./known_types.js\");\n" +
                    "\n" +
                    "let wrappers = require(\"google-protobuf/google/protobuf/wrappers_pb.js\");\n" +
                    "let struct = require(\"google-protobuf/google/protobuf/struct_pb.js\");\n" +
                    "let empty = require(\"google-protobuf/google/protobuf/empty_pb.js\");\n" +
                    "let timestamp = require('google-protobuf/google/protobuf/timestamp_pb.js');\n" +
                    "let duration = require('google-protobuf/google/protobuf/duration_pb.js');\n" +
                    "let field_mask = require('google-protobuf/google/protobuf/field_mask_pb.js');\n" +
                    "let any = require('google-protobuf/google/protobuf/any_pb.js');\n" +
                    "\n" +
                    "class BoolValueParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let boolValue = new wrappers.BoolValue();\n" +
                    "        boolValue.setValue(value);\n" +
                    "        return boolValue;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class BytesValueParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let bytesValue = new wrappers.BytesValue();\n" +
                    "        bytesValue.setValue(value);\n" +
                    "        return bytesValue;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class DoubleValueParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let doubleValue = new wrappers.DoubleValue();\n" +
                    "        doubleValue.setValue(value);\n" +
                    "        return doubleValue;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class FloatValueParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let floatValue = new wrappers.FloatValue();\n" +
                    "        floatValue.setValue(value);\n" +
                    "        return floatValue;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class Int32ValueParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let int32Value = new wrappers.Int32Value();\n" +
                    "        int32Value.setValue(value);\n" +
                    "        return int32Value;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class Int64ValueParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let int64Value = new wrappers.Int64Value();\n" +
                    "        int64Value.setValue(value);\n" +
                    "        return int64Value;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class StringValueParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let stringValue = new wrappers.StringValue();\n" +
                    "        stringValue.setValue(value);\n" +
                    "        return stringValue;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class UInt32ValueParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let uInt32Value = new wrappers.UInt32Value();\n" +
                    "        uInt32Value.setValue(value);\n" +
                    "        return uInt32Value;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class UInt64ValueParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let uInt64Value = new wrappers.UInt64Value();\n" +
                    "        uInt64Value.setValue(value);\n" +
                    "        return uInt64Value;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class ValueParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let result = new struct.Value();\n" +
                    "        if (value === null) {\n" +
                    "            result.setNullValue(struct.NullValue);\n" +
                    "        } else if (typeof value === 'number') {\n" +
                    "            result.setNumberValue(value);\n" +
                    "        } else if (typeof value === 'string') {\n" +
                    "            result.setStringValue(value);\n" +
                    "        } else if (typeof value === 'boolean') {\n" +
                    "            result.setBoolValue(value);\n" +
                    "        } else if (Array.isArray(value)) {\n" +
                    "            let parser = new ListValueParser(value);\n" +
                    "            let listValue = parser.parse(value);\n" +
                    "            result.setListValue(listValue);\n" +
                    "        } else {\n" +
                    "            // Is a Struct, unhandled for now.\n" +
                    "        }\n" +
                    "        return result;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class NullValueParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let nullValue = new struct.NullValue();\n" +
                    "        return nullValue;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class ListValueParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let listValue = new struct.ListValue;\n" +
                    "        value.forEach(\n" +
                    "            function callback(currentValue, index, array) {\n" +
                    "                let valueParser = new ValueParser();\n" +
                    "                array[index] = valueParser.parse(currentValue);\n" +
                    "            }\n" +
                    "        );\n" +
                    "        listValue.setValuesList(value);\n" +
                    "        return listValue;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class EmptyParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let empty = new empty.Empty();\n" +
                    "        return empty;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class TimestampParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let date = new Date(value);\n" +
                    "        let result = new timestamp.Timestamp();\n" +
                    "        result.fromDate(date);\n" +
                    "        return result;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class DurationParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        // Remove \"s\" symbol at the end of the string.\n" +
                    "        value = value.substring(0, value.length - 1);\n" +
                    "        let values = value.split('.');\n" +
                    "        let result = new duration.Duration();\n" +
                    "        if (values.length === 1) {\n" +
                    "            result.setSeconds(values[0]);\n" +
                    "        } else if (values.length === 2) {\n" +
                    "            result.setSeconds(values[0]);\n" +
                    "            let nanos = values[1];\n" +
                    "            for (let i = 0; i < 9 - nanos.length; i++) {\n" +
                    "                nanos += \"0\";\n" +
                    "            }\n" +
                    "            let nanosNumber = parseInt(nanos, 10);\n" +
                    "            result.setNanos(nanosNumber);\n" +
                    "        } else {\n" +
                    "            // Do nothing, should never happen.\n" +
                    "        }\n" +
                    "        return result;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class FieldMaskParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let fieldMask = new field_mask.FieldMask();\n" +
                    "        fieldMask.setPathsList(value.split(','));\n" +
                    "        return fieldMask;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "class AnyParser {\n" +
                    "\n" +
                    "    parse(value) {\n" +
                    "        let typeUrl = value['@type'];\n" +
                    "        let type = known_types.types.get(typeUrl);\n" +
                    "        let instance;\n" +
                    "        let parser = parsers.get(type);\n" +
                    "        if (parser) {\n" +
                    "            instance = parser.parse(value['value']);\n" +
                    "        } else {\n" +
                    "            let msg = new type();\n" +
                    "            instance = msg.fromObject(value);\n" +
                    "        }\n" +
                    "        let bytes = instance.serializeBinary();\n" +
                    "        let anyMsg = new any.Any;\n" +
                    "        anyMsg.setTypeUrl(typeUrl);\n" +
                    "        anyMsg.setValue(bytes);\n" +
                    "        return anyMsg;\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "export const parsers = new Map([\n" +
                    "    [wrappers.BoolValue, new BoolValueParser()],\n" +
                    "    [wrappers.BytesValue, new BytesValueParser()],\n" +
                    "    [wrappers.DoubleValue, new DoubleValueParser()],\n" +
                    "    [wrappers.FloatValue, new FloatValueParser()],\n" +
                    "    [wrappers.Int32Value, new Int32ValueParser()],\n" +
                    "    [wrappers.Int64Value, new Int64ValueParser()],\n" +
                    "    [wrappers.StringValue, new StringValueParser()],\n" +
                    "    [wrappers.UInt32Value, new UInt32ValueParser()],\n" +
                    "    [wrappers.UInt64Value, new UInt64ValueParser()],\n" +
                    "    [struct.Value, new ValueParser()],\n" +
                    "    [struct.NullValue, new NullValueParser()],\n" +
                    "    [struct.ListValue, new ListValueParser()],\n" +
                    "    [empty.Empty, new EmptyParser()],\n" +
                    "    [timestamp.Timestamp, new TimestampParser()],\n" +
                    "    [duration.Duration, new DurationParser()],\n" +
                    "    [field_mask.FieldMask, new FieldMaskParser()],\n" +
                    "    [any.Any, new AnyParser()]\n" +
                    "]);\n";

    public static String get() {
        return CODE;
    }

    public static String mapName() {
        return "parsers";
    }
}
