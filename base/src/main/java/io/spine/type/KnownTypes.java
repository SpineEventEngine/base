/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.TypeRegistry;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.security.InvocationGuard;
import io.spine.validate.ExternalConstraints;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.toSet;

/**
 * All Protobuf types known to the application.
 *
 * <p>This includes types generated by Protobuf compiler and collected into
 * the {@linkplain io.spine.code.proto.FileDescriptors#KNOWN_TYPES resource file}.
 *
 * <p>An instance of {@link KnownTypes} is immutable. All the instance methods of this class are
 * deterministic, i.e., when called on the same instance, they return results that are equal in
 * terms of {@code Object.equals}. Though, the {@link KnownTypes#instance()} method is not
 * deterministic, i.e. can return different objects. It is guaranteed that no types are being
 * forgotten:
 * <pre>
 *     {@code
 *     KnownTypes oldTypes = KnownTypes.instance();
 *     // ...
 *     KnownTypes newTypes = KnownTypes.instance();
 *     }
 * </pre>
 *
 * <p>In the snippet above, {@code oldTypes} contains a subset or is equal to the {@code newTypes}.
 */
@Internal
@Immutable
public class KnownTypes implements Serializable {

    private static final long serialVersionUID = 0L;

    private static final Joiner NEW_LINE_JOINER = Joiner.on(lineSeparator());

    @SuppressWarnings("TransientFieldNotInitialized") // Instance is substituted on deserialization.
    private final transient TypeSet typeSet;

    /**
     * Retrieves the singleton instance of {@code KnownTypes}.
     */
    public static KnownTypes instance() {
        return Holder.instance();
    }

    private KnownTypes() {
        this(load());
    }

    private KnownTypes(TypeSet types) {
        this.typeSet = checkNotNull(types);
    }

    private Object readResolve() {
        return new KnownTypes();
    }

    private Set<Type<?, ?>> types() {
        return typeSet.allTypes();
    }

    /**
     * Loads known types from the classpath.
     */
    private static TypeSet load() {
        FileSet protoDefinitions = FileSet.load();
        TypeSet types = TypeSet.from(protoDefinitions);
        return types;
    }

    /**
     * Retrieves a Java class name generated for the Protobuf type by its type URL
     * to be used to parse {@link Message Message} from {@link Any}.
     *
     * @param type
     *         {@link Any} type URL
     * @return Java class name
     * @throws UnknownTypeException
     *         if there is no such type known to the application
     */
    public ClassName classNameOf(TypeUrl type) throws UnknownTypeException {
        if (!instance().contains(type)) {
            throw new UnknownTypeException(type.toTypeName()
                                               .value());
        }
        ClassName result = instance().get(type);
        return result;
    }

    /**
     * Retrieves Protobuf type URLs known to the application.
     */
    public Set<TypeUrl> allUrls() {
        return types().stream()
                      .map(Type::url)
                      .collect(toSet());
    }

    /**
     * Retrieves all Protobuf types known to the application.
     */
    public TypeSet asTypeSet() {
        return typeSet;
    }

    /**
     * Assembles the known types into a {@code TypeRegistry}.
     *
     * <p>The resulting registry contains all the known Protobuf message types.
     */
    public TypeRegistry typeRegistry() {
        return typeSet.toJsonPrinterRegistry();
    }

    /**
     * Retrieves all the types that belong to the given package or its subpackages.
     *
     * @param packageName
     *         proto package name
     * @return set of {@link TypeUrl TypeUrl}s of types that belong to the given package
     */
    public Set<TypeUrl> allFromPackage(String packageName) {
        Set<TypeUrl> result = allUrls().stream()
                                       .filter(url -> url.toTypeName()
                                                         .belongsTo(packageName))
                                       .collect(toSet());
        return result;
    }

    /**
     * Shows if the given {@link TypeUrl} is known the system.
     *
     * @param typeUrl
     *         the {@code TypeUrl} to look up
     * @return {@code true} if the given type is known, {@code false} otherwise
     */
    public boolean contains(TypeUrl typeUrl) {
        TypeName name = typeUrl.toTypeName();
        boolean result = typeSet.contains(name);
        return result;
    }

    /**
     * Finds a {@link Type} by its name.
     *
     * @see TypeSet#find(TypeName)
     */
    Optional<Type<?, ?>> find(TypeName typeName) {
        Optional<Type<?, ?>> type = typeSet.find(typeName);
        return type;
    }

    private Type<?, ?> get(TypeName name) throws UnknownTypeException {
        Type<?, ?> result = typeSet.find(name)
                                   .orElseThrow(() -> new UnknownTypeException(name.value()));
        return result;
    }

    private ClassName get(TypeUrl typeUrl) {
        Type<?, ?> type = get(typeUrl.toTypeName());
        ClassName result = type.javaClassName();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(KnownTypes.class.getSimpleName());
        result.append(':')
              .append(lineSeparator());
        NEW_LINE_JOINER.appendTo(result, allUrlList());
        return result.toString();
    }

    /**
     * Prints alphabetically sorted URLs of the known types, having each type on a separate line.
     */
    public String printAllTypes() {
        return NEW_LINE_JOINER.join(allUrlList());
    }

    /**
     * Obtains alphabetically sorted list of URLs of all known types.
     */
    private List<String> allUrlList() {
        ImmutableList<String> result = allUrls()
                .stream()
                .map(TypeUrl::value)
                .sorted()
                .collect(toImmutableList());
        return result;
    }

    /**
     * A holder of the {@link KnownTypes} instance.
     *
     * @apiNote This class is public for allowing extension of known types by the
     *         development tools.
     */
    @Internal
    public static final class Holder {

        private static final FluentLogger logger = FluentLogger.forEnclosingClass();

        /** The lock to synchronize the write access to the {@code KnownTypes} instance. */
        private static final Lock lock = new ReentrantLock(false);

        /** The singleton instance. */
        private static KnownTypes instance = new KnownTypes();

        /** Prevents instantiation from outside. */
        private Holder() {
        }

        /** Retrieves the singleton instance of {@code KnownTypes}. */
        private static KnownTypes instance() {
            return instance;
        }

        /**
         * Extends the known types with some more types.
         *
         * <p>Triggers external constraints
         * {@link ExternalConstraints#updateFrom(ImmutableSet) update}.
         *
         * <p>This method should never be called in a client code. The sole purpose of extending
         * the known types is for running compile-time checks on the user types.
         *
         * @throws java.lang.SecurityException
         *         if called from the client code
         */
        public static void extendWith(TypeSet moreKnownTypes) {
            InvocationGuard.allowOnly("io.spine.tools.type.MoreKnownTypes");
            logger.atFine()
                  .log("Adding types `%s` to known types.", moreKnownTypes);
            lock.lock();
            try {
                TypeSet newKnownTypes = instance.typeSet.union(moreKnownTypes);
                instance = new KnownTypes(newKnownTypes);
                ExternalConstraints.updateFrom(moreKnownTypes.messageTypes());
            } finally {
                lock.unlock();
            }
        }
    }
}
