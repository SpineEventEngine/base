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

package io.spine.testing;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.FieldMaskUtil;
import io.spine.testing.given.TestsTestEnv.ClassThrowingExceptionInConstructor;
import io.spine.testing.given.TestsTestEnv.ClassWithCtorWithArgs;
import io.spine.testing.given.TestsTestEnv.ClassWithPrivateCtor;
import io.spine.testing.given.TestsTestEnv.ClassWithPublicCtor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.HospitalPolicy.ACCEPTED_CONDITION_FIELD_NUMBER;
import static io.spine.testing.HospitalPolicy.PatientCondition.CRITICAL;
import static io.spine.testing.HospitalPolicy.PatientCondition.CRITICAL_BUT_STABLE;
import static io.spine.testing.Prescription.PRESCRIBED_DRUG_FIELD_NUMBER;
import static io.spine.testing.Prescription.PRESCRIBED_ON_FIELD_NUMBER;
import static io.spine.testing.PrescriptionHistory.PRESCRIPTION_RECEIVER_FIELD_NUMBER;
import static io.spine.testing.PrescriptionHistory.RECEIVED_PRESCRIPTION_FIELD_NUMBER;
import static io.spine.testing.Tests.assertInDelta;
import static io.spine.testing.Tests.assertMatchesMask;
import static io.spine.testing.Tests.hasPrivateParameterlessCtor;
import static io.spine.testing.Tests.repeat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests utility class should")
class TestsTest extends UtilityClassTest<Tests> {

    TestsTest() {
        super(Tests.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        tester.setDefault(FieldMask.class, FieldMask.getDefaultInstance());
    }

    @Nested
    @DisplayName("Check private parameterless constructor")
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

    @Test
    @DisplayName("provide null reference method")
    void nullRef() {
        assertNull(Tests.nullRef());
    }

    @Nested
    @DisplayName("Assert boolean equality")
    class BooleanAssert {

        @Test
        @DisplayName("when true")
        void onTrue() {
            Tests.assertEquals(true, true);
        }

        @Test
        @DisplayName("when false")
        void onFalse() {
            Tests.assertEquals(false, false);
        }

        @Test
        @DisplayName("fail when not equal")
        void failInequality() {
            assertThrows(
                    AssertionError.class,
                    () -> Tests.assertEquals(true, false)
            );
        }
    }

    @Nested
    @DisplayName("Assert true")
    class AssertTrue {

        @Test
        @DisplayName("when true")
        void onTrue() {
            Tests.assertTrue(true);
        }

        @Test
        @DisplayName("fail when false")
        void whenFalse() {
            assertThrows(
                    AssertionError.class,
                    () -> Tests.assertTrue(false)
            );
        }
    }

    @Nested
    @DisplayName("Assert matches mask")
    class AssertMatchesMask {

        private Timestamp timestampMsg;

        @BeforeEach
        void setUp() {
            long currentTime = Instant.now()
                                      .toEpochMilli();
            timestampMsg = Timestamp.newBuilder()
                                    .setSeconds(currentTime)
                                    .build();
        }

        @Test
        @DisplayName("when field is matched")
        void fieldIsPresent() {
            FieldMask fieldMask = FieldMaskUtil.fromFieldNumbers(Timestamp.class, 1);

            assertMatchesMask(timestampMsg, fieldMask);
        }

        @Test
        @DisplayName("throws the error when field is not present")
        void fieldIsNotPresent() {
            FieldMask fieldMask = FieldMaskUtil.fromFieldNumbers(Any.class, 1);

            assertThrows(AssertionError.class, () -> assertMatchesMask(timestampMsg, fieldMask));
        }

        @Test
        @DisplayName("throws the error when the field value is not set")
        void fieldIsNotSet() {
            String fieldPath = Timestamp.getDescriptor()
                                        .getFields()
                                        .get(0)
                                        .getFullName();
            FieldMask.Builder builder = FieldMask.newBuilder();
            builder.addPaths(fieldPath);
            FieldMask fieldMask = builder.build();

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
                Prescription prescription = prescribeFromCold();
                FieldMask fieldMask = FieldMaskUtil.fromFieldNumbers(Prescription.class,
                                                                     PRESCRIBED_DRUG_FIELD_NUMBER,
                                                                     PRESCRIBED_ON_FIELD_NUMBER);
                assertMatchesMask(prescription, fieldMask);
            }

            @DisplayName("not match absent repeated primitive fields")
            @Test
            void notMatchAbsentRepeatedPrimitiveFields() {
                Prescription emptyPrescription = Prescription.getDefaultInstance();
                FieldMask fieldMask = FieldMaskUtil.fromFieldNumbers(Prescription.class,
                                                                     PRESCRIBED_DRUG_FIELD_NUMBER);
                assertThrows(AssertionError.class,
                             () -> assertMatchesMask(emptyPrescription, fieldMask));
            }

            @DisplayName("match existing repeated non-default message fields")
            @Test
            void matchRepeatedNonDefaultMessageFields() {
                PrescriptionHistory history = PrescriptionHistory
                        .newBuilder()
                        .addReceivedPrescription(prescribeFromCold())
                        .setPrescriptionReceiver(patientId)
                        .build();
                FieldMask fieldMask =
                        FieldMaskUtil.fromFieldNumbers(PrescriptionHistory.class,
                                                       PRESCRIPTION_RECEIVER_FIELD_NUMBER,
                                                       RECEIVED_PRESCRIPTION_FIELD_NUMBER);
                assertMatchesMask(history, fieldMask);
            }

            @DisplayName("not match absent repeated message fields")
            @Test
            void notMatchAbsentRepeatedMessageFields() {
                PrescriptionHistory history = PrescriptionHistory
                        .newBuilder()
                        .setPrescriptionReceiver(patientId)
                        .build();

                FieldMask fieldMask =
                        FieldMaskUtil.fromFieldNumbers(PrescriptionHistory.class,
                                                       PRESCRIPTION_RECEIVER_FIELD_NUMBER,
                                                       RECEIVED_PRESCRIPTION_FIELD_NUMBER);
                assertThrows(AssertionError.class, () -> assertMatchesMask(history, fieldMask));
            }

            @DisplayName("match existing repeated enum fields")
            @Test
            void matchPresentRepeatedEnumFields() {
                HospitalPolicy policy = HospitalPolicy
                        .newBuilder()
                        .addAcceptedCondition(CRITICAL)
                        .addAcceptedCondition(CRITICAL_BUT_STABLE)
                        .build();

                FieldMask fieldMask =
                        FieldMaskUtil.fromFieldNumbers(HospitalPolicy.class,
                                                       ACCEPTED_CONDITION_FIELD_NUMBER);
                assertMatchesMask(policy, fieldMask);
            }

            @DisplayName("not match absent repeated enum fields")
            @Test
            void notMatchAbsentRepeatedEnumFields() {
                HospitalPolicy emptyPolicy = HospitalPolicy.getDefaultInstance();
                FieldMask fieldMask =
                        FieldMaskUtil.fromFieldNumbers(HospitalPolicy.class,
                                                       ACCEPTED_CONDITION_FIELD_NUMBER);
                assertThrows(AssertionError.class, () -> assertMatchesMask(emptyPolicy, fieldMask));
            }

            private Prescription prescribeFromCold() {
                long currentTime = Instant.now()
                                          .toEpochMilli();
                Timestamp now = Timestamp
                        .newBuilder()
                        .setSeconds(currentTime)
                        .build();
                return Prescription
                        .newBuilder()
                        .setPrescribedOn(now)
                        .addPrescribedDrug("Paracetamol")
                        .addPrescribedDrug("Aspirin")
                        .addPrescribedDrug("Tylenol")
                        .build();
            }

            private PatientId newPatient() {
                String uuid = UUID.randomUUID()
                                  .toString();
                return PatientId
                        .newBuilder()
                        .setValue(uuid)
                        .build();
            }
        }
    }

    @Nested
    @DisplayName("Assert values in delta")
    class AssertInDelta {

        private static final long DELTA = 10;
        private static final long VALUE = 100;

        private long getValue() {
            return VALUE;
        }

        @Test
        @DisplayName("when values are equal")
        void equalValues() {
            long expectedValue = getValue();
            @SuppressWarnings("UnnecessaryLocalVariable") // For readability of this test.
            long actualValue = expectedValue;
            assertInDelta(expectedValue, actualValue, 0);
        }

        @Test
        @DisplayName("when values are close")
        void closeValues() {
            long expectedValue = getValue();
            long actualValue = getValue();
            assertInDelta(expectedValue, actualValue, DELTA);
        }

        @Test
        @DisplayName("when actual value less than the sum of the expected value and delta")
        void actualValueLessThanExpectedWithDelta() {
            long expectedValue = getValue();
            long actualValue = expectedValue + DELTA - 1;
            assertInDelta(expectedValue, actualValue, DELTA);
        }

        @Test
        @DisplayName("when the actual value equals the sum of the expected value and delta")
        void actualValueEqualsTheSumExpectedValueAndDelta() {
            long expectedValue = getValue();
            long actualValue = expectedValue + DELTA;
            assertInDelta(expectedValue, actualValue, DELTA);
        }

        @Test
        @DisplayName("throw when the actual value greater than the sum of the expected value and delta")
        void actualValueGreaterThanTheSumExpectedValueAndDelta() {
            long expectedValue = getValue();
            long actualValue = expectedValue + DELTA + 1;
            assertThrows(
                    AssertionError.class,
                    () -> assertInDelta(expectedValue, actualValue, DELTA)
            );
        }

        @Test
        @DisplayName("when the actual value greater than the subtraction of the expected value and delta")
        void actualValueGreaterThanTheSubtractionOfExpectedValueAndDelta() {
            long actualValue = getValue();
            long expectedValue = actualValue + DELTA - 1;
            assertInDelta(expectedValue, actualValue, DELTA);
        }

        @Test
        @DisplayName("when the actual value equals the subtraction of the expected value and delta")
        void actualValueEqualsTheSubtractionOfExpectedValueAndDelta() {
            long actualValue = getValue();
            long expectedValue = actualValue + DELTA;
            assertInDelta(expectedValue, actualValue, DELTA);
        }

        @Test
        @DisplayName("throw when the actual value less than the subtraction of the expected value and delta")
        void actualValueLessThanTheSubtractionExpectedValueAndDelta() {
            long actualValue = getValue();
            long expectedValue = actualValue + DELTA + 1;
            assertThrows(
                    AssertionError.class,
                    () -> assertInDelta(expectedValue, actualValue, DELTA)
            );
        }
    }

    @Test
    @DisplayName("repeat an action a number of times")
    void repeating() {
        int expected = TestValues.random(10);
        AtomicInteger counter = new AtomicInteger(0);
        repeat(expected, counter::incrementAndGet);

        assertThat(counter.get())
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("provide method `halt()` for failing methods that should never be called")
    void haltMethod() {
        assertThrows(AssertionError.class, Tests::halt);
    }
}
