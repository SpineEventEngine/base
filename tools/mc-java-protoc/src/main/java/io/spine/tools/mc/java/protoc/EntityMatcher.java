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

package io.spine.tools.mc.java.protoc;

import io.spine.tools.protoc.ForEntities;
import io.spine.type.MessageType;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A predicate which checks if a {@link MessageType} represents an entity state type.
 *
 * <p>An entity state type may be marked by a Protobuf option or be matched by a file pattern.
 */
public final class EntityMatcher implements Predicate<MessageType> {

    private final Predicate<MessageType> matcher;

    public EntityMatcher(ForEntities entities) {
        checkNotNull(entities);
        matcher = matchAgainst(entities.getOptionList(), OptionMatcher::new)
                .or(matchAgainst(entities.getPatternList(), FilePatternMatcher::new));
    }

    private static <T> Predicate<MessageType>
    matchAgainst(List<T> criteria, Function<T, Predicate<MessageType>> newMatcher) {
        return type -> criteria.stream()
                               .map(newMatcher)
                               .anyMatch(matcher -> matcher.test(type));
    }

    @Override
    public boolean test(MessageType type) {
        return matcher.test(type);
    }
}
