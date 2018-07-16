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

package io.spine.protobuf;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import io.spine.base.Time;
import io.spine.test.protobuf.Associations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Attribute should")
class AttributeTest {

    private static final long LONG_VAL = 10_001L;

    private final StrAttribute strAttr = new StrAttribute();
    private final BoolAttribute boolAttr = new BoolAttribute();
    private final LongAttribute longAttr = new LongAttribute();
    private final TimestampAttribute timestampAttr = new TimestampAttribute();

    private Associations msg;

    @BeforeEach
    void setUp() {
        Associations.Builder builder = Associations.newBuilder();

        boolAttr.setValue(builder, Boolean.TRUE);
        longAttr.setValue(builder, LONG_VAL);
        timestampAttr.setValue(builder, Time.getCurrentTime());

        msg = builder.build();
    }

    @Test
    @DisplayName("return empty Optional if not set")
    void whenNotSet() {
        assertFalse(strAttr.getValue(msg)
                           .isPresent());
    }

    @Test
    @DisplayName("store string value")
    void storeString() {
        Associations.Builder builder = msg.toBuilder();
        String expected = getClass().getName();
        strAttr.setValue(builder, expected);

        Associations updated = builder.build();

        Optional<String> value = strAttr.getValue(updated);
        assertTrue(value.isPresent());
        
        assertEquals(expected, value.get());
    }

    @Test
    @DisplayName("obtain boolean value")
    void obtainBooleanValue() {
        assertTrue(boolAttr.getValue(msg)
                           .isPresent());
        assertTrue(boolAttr.getValue(msg)
                           .get());
    }

    @Test
    @DisplayName("obtain long value")
    void obtainLongValue() {
        assertTrue(longAttr.getValue(msg)
                           .isPresent());
        assertEquals(LONG_VAL, longAttr.getValue(msg)
                                       .get()
                                       .longValue());
    }

    @Test
    @DisplayName("obtain message value")
    void obtainMessageValue() {
        Optional<Timestamp> value = timestampAttr.getValue(msg);
        assertTrue(value.isPresent());
        assertNotEquals(Timestamp.getDefaultInstance(), value.get());
    }

    /**
     * The abstract base for attributes stored in the map under {@link Associations} message.
     *
     * @param <T> the type of the attribute
     */
    abstract static class AssociationAttribute<T>
            extends Attribute<T, Associations, Associations.Builder> {

        protected AssociationAttribute(String name) {
            super(name);
        }

        @Override
        protected Map<String, Any> getMap(Associations obj) {
            return obj.getEntriesMap();
        }

        @Override
        protected Map<String, Any> getMutableMap(Associations.Builder builder) {
            return builder.getMutableEntries();
        }
    }

    private static final class BoolAttribute extends AssociationAttribute<Boolean> {

        BoolAttribute() {
            super("bool-value");
        }
    }

    static final class LongAttribute extends AssociationAttribute<Long> {

        LongAttribute() {
            super("long-value");
        }
    }

    static final class StrAttribute extends AssociationAttribute<String> {

        StrAttribute() {
            super("string-value");
        }
    }

    static final class TimestampAttribute extends AssociationAttribute<Timestamp> {

        TimestampAttribute() {
            super("timestamp-value");
        }
    }
}
