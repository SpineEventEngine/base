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

import com.google.common.collect.ImmutableList;
import io.spine.annotation.Internal;
import io.spine.base.CommandMessage;
import io.spine.base.EntityState;
import io.spine.base.EventMessage;
import io.spine.base.EventMessageField;
import io.spine.base.RejectionMessage;
import io.spine.base.UuidValue;
import io.spine.option.OptionsProto;
import io.spine.query.EntityStateField;
import io.spine.tools.java.code.UuidMethodFactory;
import io.spine.tools.protoc.Classpath;
import io.spine.tools.protoc.FilePattern;
import io.spine.tools.protoc.ForMessages;
import io.spine.tools.protoc.Pattern;
import io.spine.tools.protoc.ProtoTypeName;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.TypePattern;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.base.MessageFile.COMMANDS;
import static io.spine.base.MessageFile.EVENTS;
import static io.spine.base.MessageFile.REJECTIONS;

/**
 * A configuration for the Model Compiler code generation.
 */
@SuppressWarnings("OverlyCoupledClass") // OK for a Gradle config.
public final class Codegen extends Config<SpineProtocConfig> {

    private final SignalConfig forCommands;
    private final SignalConfig forEvents;
    private final SignalConfig forRejections;
    private final EntityConfig forEntities;
    private final UuidConfig forUuids;
    private final ValidationConfig validation;
    private final Set<ForMessages> forMessagesConfigs = new HashSet<>();
    private final Project project;

    @Internal
    public Codegen(Project project) {
        super();
        this.project = checkNotNull(project);
        this.forCommands = new SignalConfig(project);
        this.forEvents = new SignalConfig(project);
        this.forRejections = new SignalConfig(project);
        this.forEntities = new EntityConfig(project);
        this.forUuids = new UuidConfig(project);
        this.validation = new ValidationConfig(project);
        prepareConvention();
    }

    private void prepareConvention() {
        forCommands.convention(COMMANDS, CommandMessage.class);
        forEvents.convention(EVENTS, EventMessage.class, EventMessageField.class);
        forRejections.convention(REJECTIONS, RejectionMessage.class, EventMessageField.class);
        forEntities.convention(OptionsProto.entity, EntityState.class, EntityStateField.class);
        forUuids.convention(UuidMethodFactory.class, UuidValue.class);
        validation.enableAllByConvention();
    }

    /**
     * Configures code generation for command messages.
     */
    public void forCommands(Action<SignalConfig> action) {
        action.execute(forCommands);
    }

    /**
     * Configures code generation for event messages.
     *
     * <p>Configuration applied to events does not automatically apply to rejections as well.
     */
    public void forEvents(Action<SignalConfig> action) {
        action.execute(forEvents);
    }

    /**
     * Configures code generation for rejection messages.
     *
     * <p>Configuration applied to events does not automatically apply to rejections as well.
     */
    public void forRejections(Action<SignalConfig> action) {
        action.execute(forRejections);
    }

    /**
     * Configures code generation for entity state messages.
     */
    public void forEntities(Action<EntityConfig> action) {
        action.execute(forEntities);
    }

    /**
     * Configures code generation for UUID messages.
     */
    public void forUuids(Action<UuidConfig> action) {
        action.execute(forUuids);
    }

    /**
     * Configures code generation for validation messages.
     */
    public void validation(Action<ValidationConfig> action) {
        action.execute(validation);
    }

    /**
     * Configures code generation for a group messages.
     *
     * <p>The group is defined by a file-based selector.
     *
     * @see #by() for creating a file pattern for selecting messages
     */
    public void forMessages(FilePattern filePattern, Action<MessagesConfig> action) {
        Pattern pattern = Pattern
                .newBuilder()
                .setFile(filePattern)
                .build();
        MessagesConfig config = new MessagesConfig(project, pattern);
        action.execute(config);
        forMessagesConfigs.add(config.toProto());
    }

    /**
     * Obtains an instance of {@link PatternFactory} which creates file patterns.
     *
     * @see #forMessages
     */
    public PatternFactory by() {
        return PatternFactory.instance();
    }

    /**
     * Configures code generation for particular message.
     */
    public void forMessage(String protoTypeName, Action<MessagesConfig> action) {
        ProtoTypeName name = ProtoTypeName.newBuilder()
                .setValue(protoTypeName)
                .build();
        Pattern pattern = Pattern.newBuilder()
                .setType(TypePattern.newBuilder().setExpectedType(name))
                .build();
        MessagesConfig config = new MessagesConfig(project, pattern);
        config.emptyByConvention();
        action.execute(config);
        forMessagesConfigs.add(config.toProto());
    }

    @Override
    public SpineProtocConfig toProto() {
        SpineProtocConfig.Builder builder = SpineProtocConfig.newBuilder()
                .setCommands(forCommands.toProto())
                .setEvents(forEvents.toProto())
                .setRejections(forRejections.toProto())
                .setEntities(forEntities.toProto())
                .setValidation(validation.toProto())
                .setUuids(forUuids.toProto())
                .setClasspath(buildClasspath());
        for (ForMessages forMessages : forMessagesConfigs) {
            builder.addMessages(forMessages);
        }
        return builder.build();
    }

    private Classpath buildClasspath() {
        Classpath.Builder classpath = Classpath.newBuilder();
        Collection<JavaCompile> javaCompileViews = project.getTasks()
                                                          .withType(JavaCompile.class);
        ImmutableList.copyOf(javaCompileViews)
                     .stream()
                     .map(JavaCompile::getClasspath)
                     .map(FileCollection::getFiles)
                     .flatMap(Set::stream)
                     .map(File::getAbsolutePath)
                     .forEach(classpath::addJar);
        return classpath.build();
    }
}
