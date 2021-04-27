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

package io.spine.tools.java.protoc;

import com.google.common.truth.Truth;
import io.spine.base.SubscribableField;
import io.spine.query.EntityStateField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("`GeneratedFields` should")
class FieldsTest {

    private static final String ENTITY_STATE_FIELD = EntityStateField.class.getCanonicalName();
    private static final String GENERIC_FIELD = SubscribableField.class.getCanonicalName();

    @Test
    @DisplayName("convert to proper Protoc configuration")
    void convertToProperProtocConfiguration() {
        Fields config = new Fields();
        MessageSelectorFactory messages = config.messages();
        config.generateFor(messages.entityState(), config.markAs(ENTITY_STATE_FIELD));
        config.generateFor(messages.inFiles(MessageSelectorFactory.suffix("_test.proto")),
                           config.markAs(GENERIC_FIELD));
        config.generateFor("some.custom.Type", config.markAs(GENERIC_FIELD));

        AddFields protocConfig = config.asProtocConfig();

        Truth.assertThat(protocConfig.getEntityStateConfig().getValue())
             .isEqualTo(ENTITY_STATE_FIELD);
        Truth.assertThat(protocConfig.getConfigByPatternCount())
             .isEqualTo(1);
        Truth.assertThat(protocConfig.getConfigByPattern(0).getValue())
             .isEqualTo(GENERIC_FIELD);
        Truth.assertThat(protocConfig.getConfigByTypeCount())
             .isEqualTo(1);
        Truth.assertThat(protocConfig.getConfigByType(0).getValue())
             .isEqualTo(GENERIC_FIELD);
    }

    @Test
    @DisplayName("add multiple file patterns")
    void addMultipleFilePatterns() {
        String pattern = "a_file_name_pattern";

        Fields config = new Fields();
        MessageSelectorFactory messages = config.messages();
        config.generateFor(messages.inFiles(MessageSelectorFactory.suffix(pattern)), config.markAs(GENERIC_FIELD));
        config.generateFor(messages.inFiles(MessageSelectorFactory.prefix(pattern)), config.markAs(GENERIC_FIELD));
        config.generateFor(messages.inFiles(MessageSelectorFactory.regex(pattern)), config.markAs(GENERIC_FIELD));

        AddFields protocConfig = config.asProtocConfig();

        Truth.assertThat(protocConfig.getConfigByPatternCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("add multiple type patterns")
    void addMultipleTypePatterns() {
        Fields config = new Fields();
        String type1 = "some.protobuf.Type1";
        String type2 = "some.protobuf.Type2";
        config.generateFor(type1, config.markAs(GENERIC_FIELD));
        config.generateFor(type2, config.markAs(GENERIC_FIELD));

        AddFields protocConfig = config.asProtocConfig();

        Truth.assertThat(protocConfig.getConfigByTypeCount()).isEqualTo(2);
    }
}
