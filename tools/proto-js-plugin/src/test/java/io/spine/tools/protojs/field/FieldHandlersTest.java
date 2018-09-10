/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.protojs.field;

import com.google.common.testing.NullPointerTester;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FieldHandlers utility should")
class FieldHandlersTest extends UtilityClassTest<FieldHandlers> {

    protected FieldHandlersTest(Class<FieldHandlers> aClass) {
        super(aClass);
    }

    @Override
    protected void setDefaults(NullPointerTester tester) {
    }

    @Test
    @DisplayName("create map handler for map Protobuf field")
    void createMapHandler() {

    }

    @Test
    @DisplayName("create repeated handler for repeated Protobuf field")
    void createRepeatedHandler() {

    }

    @Test
    @DisplayName("create singular handler for ordinary Protobuf field")
    void createSingularHandler() {

    }

    @Test
    @DisplayName("set value checker of correct type for handler")
    void setValueChecker() {

    }

    @Test
    @DisplayName("set value parser of correct type for handler")
    void setValueParser() {

    }

    @Test
    @DisplayName("create value parser for key and value in case of map field")
    void setParsersForMapField() {

    }
}
