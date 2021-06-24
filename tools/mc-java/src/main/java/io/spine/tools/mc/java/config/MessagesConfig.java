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

package io.spine.tools.mc.java.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.spine.tools.protoc.ForMessages;
import io.spine.tools.protoc.GenerateMethods;
import io.spine.tools.protoc.GenerateNestedClasses;
import io.spine.tools.protoc.MethodFactoryName;
import io.spine.tools.protoc.NestedClassFactoryName;
import io.spine.tools.protoc.Pattern;
import org.gradle.api.Project;
import org.gradle.api.provider.SetProperty;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

public final class MessagesConfig extends ConfigWithInterfaces<ForMessages> {

    private final Pattern pattern;
    private final SetProperty<String> methodFactories;
    private final SetProperty<String> nestedClassFactories;

    MessagesConfig(Project p, Pattern pattern) {
        super(p);
        this.pattern = pattern;
        methodFactories = p.getObjects()
                           .setProperty(String.class)
                           .convention(ImmutableList.of());
        nestedClassFactories = p.getObjects()
                                .setProperty(String.class)
                                .convention(ImmutableList.of());
    }

    void emptyByConvention() {
        interfaceNames().convention(ImmutableSet.of());
        methodFactories.convention(ImmutableSet.of());
        nestedClassFactories.convention(ImmutableSet.of());
    }

    public void generateMethodsWith(String factoryClassName) {
        methodFactories.add(factoryClassName);
    }

    public void generateNestedClassesWith(String factoryClassName) {
        nestedClassFactories.add(factoryClassName);
    }

    @Override
    ForMessages toProto() {
        return ForMessages.newBuilder()
                .setPattern(pattern)
                .addAllAddInterface(interfaces())
                .addAllGenerateMethods(generateMethods())
                .addAllGenerateNestedClasses(generateNestedClasses())
                .build();
    }

    private Set<GenerateMethods> generateMethods() {
        return methodFactories.get()
                              .stream()
                              .map(name -> MethodFactoryName.newBuilder()
                                      .setClassName(className(name))
                                      .build())
                              .map(name -> GenerateMethods.newBuilder()
                                      .setFactory(name)
                                      .build())
                              .collect(toSet());
    }

    private Set<GenerateNestedClasses> generateNestedClasses() {
        return nestedClassFactories.get()
                                   .stream()
                                   .map(name -> NestedClassFactoryName.newBuilder()
                                           .setClassName(className(name))
                                           .build())
                                   .map(name -> GenerateNestedClasses.newBuilder()
                                           .setFactory(name)
                                           .build())
                                   .collect(toSet());
    }
}
