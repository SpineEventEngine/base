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

package io.spine.code.proto;

import io.spine.code.proto.ref.TypeRef;
import io.spine.option.LifecycleOption;
import io.spine.option.OptionsProto;

import java.util.Optional;
import java.util.function.Function;

public final class EntityLifecycleOption extends MessageOption<LifecycleOption> {

    public EntityLifecycleOption() {
        super(OptionsProto.lifecycle);
    }

    public boolean hasLifecycle(MessageType type) {
        Optional<LifecycleOption> option = valueFrom(type.descriptor());
        return option.isPresent();
    }

    public Optional<TypeRef> archiveUpon(MessageType type) {
        Optional<TypeRef> result = optionIfPresent(type, LifecycleOption::getArchiveUpon);
        return result;
    }

    public Optional<TypeRef> deleteUpon(MessageType type) {
        Optional<TypeRef> result = optionIfPresent(type, LifecycleOption::getDeleteUpon);
        return result;
    }

    private Optional<TypeRef> optionIfPresent(MessageType type,
                                              Function<LifecycleOption, String> optionGetter) {
        Optional<LifecycleOption> option = valueFrom(type.descriptor());
        Optional<TypeRef> result =
                option.map(optionGetter)
                      .flatMap(EntityLifecycleOption::typeRefIfPresent)
                      .map(typeRef -> ensurePackage(typeRef, type));
        return result;
    }

    private static Optional<TypeRef> typeRefIfPresent(String fieldValue) {
        Optional<TypeRef> result =
                Optional.of(fieldValue)
                        .map(String::trim)
                        .filter(str -> !str.isEmpty())
                        .map(TypeRef::parse);
        return result;
    }

    private static TypeRef ensurePackage(TypeRef typeRef, MessageType enclosing) {
        PackageName packageName = PackageName.of(enclosing.descriptor());
        TypeRef result = typeRef.withPackage(packageName);
        return result;
    }
}
