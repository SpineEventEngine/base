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

@DisplayName("Fields utility should")
class FieldsTest extends UtilityClassTest<Fields> {

    protected FieldsTest(Class<Fields> aClass) {
        super(aClass);
    }

    @Override
    protected void setDefaults(NullPointerTester tester) {
    }

    @Test
    @DisplayName("tell if field is message")
    void tellIfMessage() {

    }

    @Test
    @DisplayName("tell if field is standard type with known parser")
    void tellIfWellKnownType() {

    }

    @Test
    @DisplayName("tell if field is repeated")
    void tellIfRepeated() {

    }

    @Test
    @DisplayName("tell if field is map")
    void tellIfMap() {

    }

    @Test
    @DisplayName("not mark map field as repeated")
    void notMarkMapAsRepeated() {

    }

    @Test
    @DisplayName("get key descriptor for map field")
    void getKeyDescriptor() {

    }

    @Test
    @DisplayName("throw ISE if getting key descriptor from non-map field")
    void getKeyOnlyFromMap() {

    }

    @Test
    @DisplayName("get value descriptor for map field")
    void getValueDescriptor() {

    }

    @Test
    @DisplayName("throw ISE if getting value descriptor from non-map field")
    void getValueOnlyFromMap() {

    }

    @Test
    @DisplayName("return capitalized field name")
    void getCapitalizedName() {

    }
}
