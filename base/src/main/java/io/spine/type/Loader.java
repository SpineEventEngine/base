/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.type;

import com.google.common.collect.ImmutableBiMap;
import com.google.protobuf.Any;
import com.google.protobuf.Api;
import com.google.protobuf.BoolValue;
import com.google.protobuf.BytesValue;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Duration;
import com.google.protobuf.Empty;
import com.google.protobuf.EnumValue;
import com.google.protobuf.Field;
import com.google.protobuf.FieldMask;
import com.google.protobuf.FloatValue;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.Internal;
import com.google.protobuf.ListValue;
import com.google.protobuf.Method;
import com.google.protobuf.Mixin;
import com.google.protobuf.NullValue;
import com.google.protobuf.Option;
import com.google.protobuf.SourceContext;
import com.google.protobuf.StringValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Syntax;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Type;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.Value;
import io.spine.Resources;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static io.spine.util.PropertyFiles.loadAllProperties;

/**
 * The helper class for building internal immutable typeUrl-to-JavaClass map.
 *
 * @author Mikhail Mikhaylov
 * @author Alexander Yevsyukov
 */
final class Loader {

    private final Map<TypeUrl, ClassName> resultMap = newHashMap();

    /** Prevents construction from outside. */
    private Loader() {
    }

    static ImmutableBiMap<TypeUrl, ClassName> load() {
        final Loader loader = new Loader().addStandardProtobufTypes()
                                          .loadResourceFile();
        final ImmutableBiMap<TypeUrl, ClassName> result = ImmutableBiMap.copyOf(loader.resultMap);
        return result;
    }

    private Loader loadResourceFile() {
        final Set<Properties> propertiesSet = loadAllProperties(Resources.KNOWN_TYPES);
        for (Properties properties : propertiesSet) {
            putProperties(properties);
        }
        return this;
    }

    private void putProperties(Properties properties) {
        final Set<String> typeUrls = properties.stringPropertyNames();
        for (String typeUrlStr : typeUrls) {
            final TypeUrl typeUrl = TypeUrl.parse(typeUrlStr);
            final ClassName className = ClassName.of(properties.getProperty(typeUrlStr));
            put(typeUrl, className);
        }
    }

    /**
     * Returns classes from the {@code com.google.protobuf} package that need to be present
     * in the result map.
     *
     * <p>This method needs to be updated with introduction of new Google Protobuf types
     * after they are used in the framework.
     */
    @SuppressWarnings("OverlyLongMethod")
    // OK as there are many types in Protobuf and we want to keep this code in one place.
    private Loader addStandardProtobufTypes() {
        // Types from `any.proto`.
        put(Any.class);

        // Types from `api.proto`
        put(Api.class);
        put(Method.class);
        put(Mixin.class);

        // Types from `descriptor.proto`
        put(DescriptorProtos.FileDescriptorSet.class);
        put(DescriptorProtos.FileDescriptorProto.class);
        put(DescriptorProtos.DescriptorProto.class);
        // Inner types of `DescriptorProto`
        put(DescriptorProtos.DescriptorProto.ExtensionRange.class);
        put(DescriptorProtos.DescriptorProto.ReservedRange.class);

        put(DescriptorProtos.FieldDescriptorProto.class);
        putEnum(DescriptorProtos.FieldDescriptorProto.Type.getDescriptor(),
                DescriptorProtos.FieldDescriptorProto.Type.class);
        putEnum(DescriptorProtos.FieldDescriptorProto.Label.getDescriptor(),
                DescriptorProtos.FieldDescriptorProto.Label.class);

        put(DescriptorProtos.OneofDescriptorProto.class);
        put(DescriptorProtos.EnumDescriptorProto.class);
        put(DescriptorProtos.EnumValueDescriptorProto.class);
        put(DescriptorProtos.ServiceDescriptorProto.class);
        put(DescriptorProtos.MethodDescriptorProto.class);
        put(DescriptorProtos.FileOptions.class);
        putEnum(DescriptorProtos.FileOptions.OptimizeMode.getDescriptor(),
                DescriptorProtos.FileOptions.OptimizeMode.class);
        put(DescriptorProtos.MessageOptions.class);
        put(DescriptorProtos.FieldOptions.class);
        putEnum(DescriptorProtos.FieldOptions.CType.getDescriptor(),
                DescriptorProtos.FieldOptions.CType.class);
        putEnum(DescriptorProtos.FieldOptions.JSType.getDescriptor(),
                DescriptorProtos.FieldOptions.JSType.class);
        put(DescriptorProtos.EnumOptions.class);
        put(DescriptorProtos.EnumValueOptions.class);
        put(DescriptorProtos.ServiceOptions.class);
        put(DescriptorProtos.MethodOptions.class);
        put(DescriptorProtos.UninterpretedOption.class);
        put(DescriptorProtos.SourceCodeInfo.class);
        // Inner types of `SourceCodeInfo`.
        put(DescriptorProtos.SourceCodeInfo.Location.class);
        put(DescriptorProtos.GeneratedCodeInfo.class);
        // Inner types of `GeneratedCodeInfo`.
        put(DescriptorProtos.GeneratedCodeInfo.Annotation.class);

        // Types from `duration.proto`.
        put(Duration.class);

        // Types from `empty.proto`.
        put(Empty.class);

        // Types from `field_mask.proto`.
        put(FieldMask.class);

        // Types from `source_context.proto`.
        put(SourceContext.class);

        // Types from `struct.proto`.
        put(Struct.class);
        put(Value.class);
        putEnum(NullValue.getDescriptor(), NullValue.class);
        put(ListValue.class);

        // Types from `timestamp.proto`.
        put(Timestamp.class);

        // Types from `type.proto`.
        put(Type.class);
        put(Field.class);
        putEnum(Field.Kind.getDescriptor(), Field.Kind.class);
        putEnum(Field.Cardinality.getDescriptor(), Field.Cardinality.class);
        put(com.google.protobuf.Enum.class);
        put(EnumValue.class);
        put(Option.class);
        putEnum(Syntax.getDescriptor(), Syntax.class);

        // Types from `wrappers.proto`.
        put(DoubleValue.class);
        put(FloatValue.class);
        put(Int64Value.class);
        put(UInt64Value.class);
        put(Int32Value.class);
        put(UInt32Value.class);
        put(BoolValue.class);
        put(StringValue.class);
        put(BytesValue.class);

        return this;
    }

    private void put(Class<? extends GeneratedMessageV3> clazz) {
        final TypeUrl typeUrl = TypeUrl.of(clazz);
        final ClassName className = ClassName.of(clazz);
        put(typeUrl, className);
    }

    private void putEnum(Descriptors.EnumDescriptor desc,
                         Class<? extends Internal.EnumLite> enumClass) {
        final TypeUrl typeUrl = TypeUrl.from(desc);
        final ClassName className = ClassName.of(enumClass);
        put(typeUrl, className);
    }

    private void put(TypeUrl typeUrl, ClassName className) {
        if (resultMap.containsKey(typeUrl)) {
            // No worries;
            // probably `task.descriptorSetOptions.includeImports` is set to `true`.
            return;
        }
        resultMap.put(typeUrl, className);
    }
}
