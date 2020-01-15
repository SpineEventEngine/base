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

package io.spine.tools.protoc;

import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protoc.ProtocTaskConfigs.queryableConfig;
import static io.spine.tools.protoc.ProtocTaskConfigs.subscribableConfig;

public final class GeneratedNestedClasses extends GeneratedConfigurations<AddNestedClasses> {

    private QueryableConfig queryableConfig = QueryableConfig.getDefaultInstance();
    private SubscribableConfig subscribableConfig  = SubscribableConfig.getDefaultInstance();

    public final void applyFactory(@FullyQualifiedName String factory, PatternSelector selector) {
        checkNotNull(factory);
        checkNotNull(selector);
        addPattern(selector, ClassName.of(factory));
    }

    public final void applyFactory(@FullyQualifiedName String factory, QueryableMessage selector) {
        checkNotNull(selector);
        queryableConfig = queryableConfig(ClassName.of(factory));
    }

    public final void
    applyFactory(@FullyQualifiedName String factory, SubscribableMessage selector) {
        checkNotNull(selector);
        subscribableConfig = subscribableConfig(ClassName.of(factory));
    }

    @Internal
    @Override
    public AddNestedClasses asProtocConfig() {
        AddNestedClasses.Builder result = AddNestedClasses
                .newBuilder()
                .setQueryableFactory(queryableConfig)
                .setSubscribableFactory(subscribableConfig);
        patternConfigurations()
                .stream()
                .map(GeneratedConfigurations::toPatternConfig)
                .forEach(result::addFactoryByPattern);
        return result.build();
    }
}
