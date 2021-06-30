/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.java.codegen;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;
import io.spine.tools.protoc.ForUuids;
import io.spine.tools.protoc.MethodFactory;
import io.spine.tools.protoc.MethodFactoryName;
import org.gradle.api.Project;
import org.gradle.api.provider.SetProperty;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Configuration for code generation for UUID messages.
 *
 * <p>A UUID message is a message which the only {@code string} field called "uuid".
 * Such messages may represent randomized typed identifiers for entities.
 */
public final class UuidConfig extends ConfigWithInterfaces<ForUuids> {

    private final SetProperty<String> methodFactories;

    UuidConfig(Project p) {
        super(p);
        methodFactories = p.getObjects()
                           .setProperty(String.class);
    }

    void convention(Class<? extends MethodFactory> methodFactory,
                    Class<? extends Message> markerInterface) {
        methodFactories.convention(ImmutableSet.of(methodFactory.getCanonicalName()));
        interfaceNames().convention(ImmutableSet.of(markerInterface.getCanonicalName()));
    }

    @Override
    ForUuids toProto() {
        return ForUuids.newBuilder()
                .addAllMethodFactory(factories())
                .addAllAddInterface(interfaces())
                .build();
    }

    /**
     * Specifies a {@link io.spine.tools.protoc.MethodFactory} to generate methods for
     * the UUID message classes.
     *
     * <p>Calling this method multiple times will add provide factories for code generation.
     *
     * @param factoryClassName
     *         the canonical class name of the method factory
     */
    public void generateMethodsWith(String factoryClassName) {
        methodFactories.add(factoryClassName);
    }

    private List<MethodFactoryName> factories() {
        return methodFactories.get()
                              .stream()
                              .map(Names::className)
                              .map(name -> MethodFactoryName.newBuilder()
                                      .setClassName(name)
                                      .build())
                              .collect(toList());
    }
}
