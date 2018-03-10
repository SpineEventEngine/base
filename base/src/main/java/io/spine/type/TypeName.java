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

import com.google.common.base.Splitter;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.Message;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.validate.Validate.checkNotEmptyOrBlank;

/**
 * A fully-qualified Protobuf type name.
 *
 * @author Alexander Yevsyukov
 */
public class TypeName extends StringTypeValue {

    /**
     * The separator character for package names in a fully qualified proto type name.
     */
    public static final char PACKAGE_SEPARATOR = '.';

    /**
     * The splitter to separate package and type names in fully-qualified type names.
     */
    private static final Splitter packageSplitter = Splitter.on(PACKAGE_SEPARATOR);

    /**
     * The character to separate a nested type from the outer type name.
     */
    public static final char NESTED_TYPE_SEPARATOR = '.';

    /**
     * The method name for obtaining a type descriptor from a Java message class.
     */
    private static final String METHOD_GET_DESCRIPTOR = "getDescriptor";

    private TypeName(String value) {
        super(value);
    }

    private static TypeName create(String value) {
        return new TypeName(value);
    }

    /**
     * Creates new instance by the passed type name value.
     */
    public static TypeName of(String typeName) {
        checkNotNull(typeName);
        checkArgument(!typeName.isEmpty());
        return create(typeName);
    }

    /**
     * Creates instance from the passed type URL.
     */
    public static TypeName from(TypeUrl typeUrl) {
        checkNotNull(typeUrl);
        return create(typeUrl.getTypeName());
    }

    /**
     * Obtains type name for the passed message.
     */
    public static TypeName of(Message message) {
        checkNotNull(message);
        return from(TypeUrl.of(message));
    }

    /**
     * Obtains type name for the passed message class.
     */
    public static TypeName of(Class<? extends Message> cls) {
        checkNotNull(cls);
        return from(TypeUrl.of(cls));
    }

    /**
     * Obtains type name for the message type by its descriptor.
     */
    public static TypeName from(Descriptor descriptor) {
        checkNotNull(descriptor);
        return from(TypeUrl.from(descriptor));
    }

    /**
     * Returns the unqualified name of the Protobuf type, for example: {@code StringValue}.
     */
    public String getSimpleName() {
        final String typeName = value();
        final List<String> tokens = packageSplitter.splitToList(typeName);
        final String result = tokens.get(tokens.size() - 1);
        return result;
    }

    /**
     * Creates URL instance corresponding to this type name.
     */
    public TypeUrl toUrl() {
        final String typeName = value();
        final TypeUrl typeUrl = KnownTypes.getTypeUrl(typeName);
        checkState(typeUrl != null, "Unable to find URL for type: %s", typeName);
        return typeUrl;
    }

    /**
     * Returns a message {@link Class} corresponding to the Protobuf type represented
     * by this type name.
     *
     * @return the message class
     * @throws UnknownTypeException wrapping {@link ClassNotFoundException} if
     *         there is no corresponding Java class
     */
    public <T extends Message> Class<T> getJavaClass() throws UnknownTypeException {
        return KnownTypes.getJavaClass(toUrl());
    }

    /**
     * Obtains descriptor for the type.
     */
    public Descriptor getDescriptor() {
        return (Descriptor) getDescriptor(value());
    }

    /**
     * Retrieve {@link Descriptors proto descriptor} from the type name.
     *
     * @param typeName <b>valid</b> name of the desired type
     * @return {@link Descriptors proto descriptor} for given type
     * @see TypeName
     * @throws IllegalArgumentException if the name does not correspond to any known type
     */
    static GenericDescriptor getDescriptor(String typeName) {
        checkNotEmptyOrBlank(typeName, "Type name cannot be empty or blank");
        final TypeUrl typeUrl = KnownTypes.getTypeUrl(typeName);
        checkArgument(typeUrl != null, "Cannot find TypeUrl for the type name: `%s`", typeName);

        final Class<?> cls = KnownTypes.getJavaClass(typeUrl);

        final GenericDescriptor descriptor;
        try {
            @SuppressWarnings("JavaReflectionMemberAccess")
            // The method is available in generated classes.
            final java.lang.reflect.Method descriptorGetter =
                    cls.getDeclaredMethod(METHOD_GET_DESCRIPTOR);
            descriptor = (GenericDescriptor) descriptorGetter.invoke(null);
        } catch (NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException e) {
            throw newIllegalStateException(e, "Unable to get descriptor for the type %s", typeName);
        }
        return descriptor;
    }
}
