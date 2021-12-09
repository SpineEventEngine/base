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

package io.spine.testing;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.FieldMaskUtil;
import io.spine.testing.given.AssertionsTestEnv.ClassThrowingExceptionInConstructor;
import io.spine.testing.given.AssertionsTestEnv.ClassWithCtorWithArgs;
import io.spine.testing.given.AssertionsTestEnv.ClassWithPrivateCtor;
import io.spine.testing.given.AssertionsTestEnv.ClassWithPublicCtor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static io.spine.testing.Assertions.assertInDelta;
import static io.spine.testing.Assertions.assertMatchesMask;
import static io.spine.testing.Assertions.hasPrivateParameterlessCtor;
import static io.spine.testing.HospitalPolicy.ACCEPTED_CONDITION_FIELD_NUMBER;
import static io.spine.testing.HospitalPolicy.PatientCondition.CRITICAL;
import static io.spine.testing.HospitalPolicy.PatientCondition.CRITICAL_BUT_STABLE;
import static io.spine.testing.Prescription.PRESCRIBED_DRUG_FIELD_NUMBER;
import static io.spine.testing.Prescription.PRESCRIBED_ON_FIELD_NUMBER;
import static io.spine.testing.PrescriptionHistory.PRESCRIPTION_RECEIVER_FIELD_NUMBER;
import static io.spine.testing.PrescriptionHistory.RECEIVED_PRESCRIPTION_FIELD_NUMBER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`Assertions` should")
class AssertionsTest extends UtilityClassTest<Assertions> {

    private AssertionsTest() {
        super(Assertions.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        super.configure(tester);
        tester.setDefault(FieldMask.class, FieldMask.getDefaultInstance());
    }

    @Nested
    @DisplayName("check private parameterless constructor")
    class ParameterlessCtor {

        @Test
        @DisplayName("returning false if it's public")
        void publicCtor() {
            assertFalse(hasPrivateParameterlessCtor(ClassWithPublicCtor.class));
        }

        @Test
        @DisplayName("return false if no parameterless ctor found")
        void ctorWithArgs() {
            assertFalse(hasPrivateParameterlessCtor(ClassWithCtorWithArgs.class));
        }

        @Test
        @DisplayName("accepting private parameterless ctor")
        void privateCtor() {
            assertTrue(hasPrivateParameterlessCtor(ClassWithPrivateCtor.class));
        }

        @Test
        @DisplayName("ignore exceptions called thrown by the constructor")
        void ignoreExceptions() {
            assertTrue(hasPrivateParameterlessCtor(ClassThrowingExceptionInConstructor.class));
        }
    }

    @Nested
    @DisplayName("assert matches mask")
    class TestingMatchesMask {

        private Timestamp timestampMsg;

        @BeforeEach
        void setUp() {
            var currentTime = Instant.now().toEpochMilli();
            timestampMsg = Timestamp.newBuilder()
                    .setSeconds(currentTime)
                    .build();
        }

        @Test
        @DisplayName("when field is matched")
        void fieldIsPresent() {
            var fieldMask = FieldMaskUtil.fromFieldNumbers(Timestamp.class, 1);

            assertMatchesMask(timestampMsg, fieldMask);
        }

        @Test
        @DisplayName("throws the error when field is not present")
        void fieldIsNotPresent() {
            var fieldMask = FieldMaskUtil.fromFieldNumbers(Any.class, 1);

            assertThrows(AssertionError.class, () -> assertMatchesMask(timestampMsg, fieldMask));
        }

        @Test
        @DisplayName("throws the error when the field value is not set")
        void fieldIsNotSet() {
            var fieldPath = Timestamp.getDescriptor()
                                     .getFields()
                                     .get(0)
                                     .getFullName();
            var builder = FieldMask.newBuilder();
            builder.addPaths(fieldPath);
            var fieldMask = builder.build();

            assertThrows(AssertionError.class,
                         () -> assertMatchesMask(Timestamp.getDefaultInstance(), fieldMask));
        }

        @Nested
        @DisplayName("with repeated fields")
        class RepeatedFields {

            private final PatientId patientId = newPatient();

            @DisplayName("match existing repeated primitive fields")
            @Test
            void matchRepeatedPrimitiveFields() {
                var prescription = prescribeFromCold();
                var fieldMask = FieldMaskUtil.fromFieldNumbers(Prescription.class,
                                                               PRESCRIBED_DRUG_FIELD_NUMBER,
                                                               PRESCRIBED_ON_FIELD_NUMBER);
                assertMatchesMask(prescription, fieldMask);
            }

            @DisplayName("not match absent repeated primitive fields")
            @Test
            void notMatchAbsentRepeatedPrimitiveFields() {
                var emptyPrescription = Prescription.getDefaultInstance();
                var fieldMask = FieldMaskUtil.fromFieldNumbers(Prescription.class,
                                                               PRESCRIBED_DRUG_FIELD_NUMBER);
                assertThrows(AssertionError.class,
                             () -> assertMatchesMask(emptyPrescription, fieldMask));
            }

            @DisplayName("match existing repeated non-default message fields")
            @Test
            void matchRepeatedNonDefaultMessageFields() {
                var history = PrescriptionHistory.newBuilder()
                        .addReceivedPrescription(prescribeFromCold())
                        .setPrescriptionReceiver(patientId)
                        .build();
                var fieldMask =
                        FieldMaskUtil.fromFieldNumbers(PrescriptionHistory.class,
                                                       PRESCRIPTION_RECEIVER_FIELD_NUMBER,
                                                       RECEIVED_PRESCRIPTION_FIELD_NUMBER);
                assertMatchesMask(history, fieldMask);
            }

            @DisplayName("not match absent repeated message fields")
            @Test
            void notMatchAbsentRepeatedMessageFields() {
                var history = PrescriptionHistory.newBuilder()
                        .setPrescriptionReceiver(patientId)
                        .build();

                var fieldMask =
                        FieldMaskUtil.fromFieldNumbers(PrescriptionHistory.class,
                                                       PRESCRIPTION_RECEIVER_FIELD_NUMBER,
                                                       RECEIVED_PRESCRIPTION_FIELD_NUMBER);
                assertThrows(AssertionError.class, () -> assertMatchesMask(history, fieldMask));
            }

            @DisplayName("match existing repeated enum fields")
            @Test
            void matchPresentRepeatedEnumFields() {
                var policy = HospitalPolicy.newBuilder()
                        .addAcceptedCondition(CRITICAL)
                        .addAcceptedCondition(CRITICAL_BUT_STABLE)
                        .build();

                var fieldMask =
                        FieldMaskUtil.fromFieldNumbers(HospitalPolicy.class,
                                                       ACCEPTED_CONDITION_FIELD_NUMBER);
                assertMatchesMask(policy, fieldMask);
            }

            @DisplayName("not match absent repeated enum fields")
            @Test
            void notMatchAbsentRepeatedEnumFields() {
                var emptyPolicy = HospitalPolicy.getDefaultInstance();
                var fieldMask = FieldMaskUtil.fromFieldNumbers(HospitalPolicy.class,
                                                               ACCEPTED_CONDITION_FIELD_NUMBER);
                assertThrows(AssertionError.class, () -> assertMatchesMask(emptyPolicy, fieldMask));
            }

            private Prescription prescribeFromCold() {
                var currentTime = Instant.now().toEpochMilli();
                var now = Timestamp.newBuilder()
                        .setSeconds(currentTime)
                        .build();
                return Prescription.newBuilder()
                        .setPrescribedOn(now)
                        .addPrescribedDrug("Paracetamol")
                        .addPrescribedDrug("Aspirin")
                        .addPrescribedDrug("Tylenol")
                        .build();
            }

            private PatientId newPatient() {
                var uuid = UUID.randomUUID().toString();
                return PatientId.newBuilder()
                        .setValue(uuid)
                        .build();
            }
        }
    }

    @Nested
    @DisplayName("assert values in delta")
    class TestingInDelta {

        private static final long DELTA = 10;
        private static final long VALUE = 100;

        private long getValue() {
            return VALUE;
        }

        @Test
        @DisplayName("when values are equal")
        void equalValues() {
            var expectedValue = getValue();
            @SuppressWarnings("UnnecessaryLocalVariable") // For readability of this test.
            var actualValue = expectedValue;
            assertInDelta(expectedValue, actualValue, 0);
        }

        @Test
        @DisplayName("when values are close")
        void closeValues() {
            var expectedValue = getValue();
            var actualValue = getValue();
            assertInDelta(expectedValue, actualValue, DELTA);
        }

        @Test
        @DisplayName("when actual value less than the sum of the expected value and delta")
        void actualValueLessThanExpectedWithDelta() {
            var expectedValue = getValue();
            var actualValue = expectedValue + DELTA - 1;
            assertInDelta(expectedValue, actualValue, DELTA);
        }

        @Test
        @DisplayName("when the actual value equals the sum of the expected value and delta")
        void actualValueEqualsTheSumExpectedValueAndDelta() {
            var expectedValue = getValue();
            var actualValue = expectedValue + DELTA;
            assertInDelta(expectedValue, actualValue, DELTA);
        }

        @Test
        @DisplayName("throw when the actual value greater than the sum of the expected value and delta")
        void actualValueGreaterThanTheSumExpectedValueAndDelta() {
            var expectedValue = getValue();
            var actualValue = expectedValue + DELTA + 1;
            assertThrows(
                    AssertionError.class,
                    () -> assertInDelta(expectedValue, actualValue, DELTA)
            );
        }

        @Test
        @DisplayName("when the actual value greater than the subtraction of the expected value and delta")
        void actualValueGreaterThanTheSubtractionOfExpectedValueAndDelta() {
            var actualValue = getValue();
            var expectedValue = actualValue + DELTA - 1;
            assertInDelta(expectedValue, actualValue, DELTA);
        }

        @Test
        @DisplayName("when the actual value equals the subtraction of the expected value and delta")
        void actualValueEqualsTheSubtractionOfExpectedValueAndDelta() {
            var actualValue = getValue();
            var expectedValue = actualValue + DELTA;
            assertInDelta(expectedValue, actualValue, DELTA);
        }

        @Test
        @DisplayName("throw when the actual value less than the subtraction of the expected value and delta")
        void actualValueLessThanTheSubtractionExpectedValueAndDelta() {
            var actualValue = getValue();
            var expectedValue = actualValue + DELTA + 1;
            assertThrows(
                    AssertionError.class,
                    () -> assertInDelta(expectedValue, actualValue, DELTA)
            );
        }
    }
}
