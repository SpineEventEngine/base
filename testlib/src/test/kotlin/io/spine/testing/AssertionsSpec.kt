/*
 * Copyright 2022, TeamDev. All rights reserved.
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
package io.spine.testing

import com.google.common.testing.NullPointerTester
import com.google.protobuf.Any
import com.google.protobuf.FieldMask
import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import io.spine.protobuf.fromFieldNumbers
import io.spine.testing.Assertions.assertInDelta
import io.spine.testing.Assertions.assertMatchesMask
import io.spine.testing.HospitalPolicy.ACCEPTED_CONDITION_FIELD_NUMBER
import io.spine.testing.HospitalPolicy.PatientCondition
import io.spine.testing.Prescription.PRESCRIBED_DRUG_FIELD_NUMBER
import io.spine.testing.Prescription.PRESCRIBED_ON_FIELD_NUMBER
import io.spine.testing.PrescriptionHistory.PRESCRIPTION_RECEIVER_FIELD_NUMBER
import io.spine.testing.PrescriptionHistory.RECEIVED_PRESCRIPTION_FIELD_NUMBER
import io.spine.testing.given.AssertionsTestEnv.ClassThrowingExceptionInConstructor
import io.spine.testing.given.AssertionsTestEnv.ClassWithCtorWithArgs
import io.spine.testing.given.AssertionsTestEnv.ClassWithPrivateCtor
import io.spine.testing.given.AssertionsTestEnv.ClassWithPublicCtor
import java.time.Instant
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("`Assertions` should")
internal class AssertionsSpec private constructor() :
    UtilityClassTest<Assertions>(Assertions::class.java) {

    override fun configure(tester: NullPointerTester) {
        super.configure(tester)
        tester.apply {
            setDefault<FieldMask>(FieldMask.getDefaultInstance())
        }
    }

    @Nested
    internal inner class `check private parameterless constructor` {

        @Test
        fun `returning false if it's public`() = assertFalse {
            hasPrivateParameterlessCtor<ClassWithPublicCtor>()
        }

        @Test
        fun `return false if no parameterless ctor found`() = assertFalse {
            hasPrivateParameterlessCtor<ClassWithCtorWithArgs>()
        }

        @Test
        fun `accepting private parameterless ctor`() = assertTrue {
            hasPrivateParameterlessCtor<ClassWithPrivateCtor>()
        }

        @Test
        fun `ignore exceptions thrown by the called constructor`() = assertTrue {
            hasPrivateParameterlessCtor<ClassThrowingExceptionInConstructor>()
        }
    }

    @Nested
    internal inner class `assert matches mask` {

        private lateinit var timestamp: Timestamp

        @BeforeEach
        fun setUp() {
            timestamp = timestamp { seconds = Instant.now().toEpochMilli() }
        }

        @Test
        fun `when field is matched`() {
            assertMatchesMask(timestamp, fromFieldNumbers<Timestamp>(1))
        }

        @Nested
        inner class `throwing when the field` {

            @Test
            fun `is not present`() {
                assertThrows<AssertionError> {
                    assertMatchesMask(timestamp, fromFieldNumbers<Any>(1))
                }
            }

            @Test
            fun `value is not set`() {
                val fieldPath = Timestamp.getDescriptor()
                    .fields[0]
                    .fullName
                val builder = FieldMask.newBuilder()
                builder.addPaths(fieldPath)
                val fieldMask = builder.build()
                assertThrows<AssertionError> {
                    assertMatchesMask(Timestamp.getDefaultInstance(), fieldMask)
                }
            }
        }

        @Nested
        internal inner class `with repeated fields` {

            private val patientId = newPatient()

            @Nested
            inner class `match existing` {

                @Test
                fun `primitive fields`() {
                    val prescription = prescribeFromCold()
                    val fieldMask = fromFieldNumbers<Prescription>(
                        PRESCRIBED_DRUG_FIELD_NUMBER,
                        PRESCRIBED_ON_FIELD_NUMBER
                    )
                    assertMatchesMask(prescription, fieldMask)
                }

                @Test
                fun `non-default message fields`() {
                    val history = prescriptionHistory {
                        receivedPrescription.add(prescribeFromCold())
                        prescriptionReceiver = patientId
                    }
                    val fieldMask = fromFieldNumbers<PrescriptionHistory>(
                        PRESCRIPTION_RECEIVER_FIELD_NUMBER,
                        RECEIVED_PRESCRIPTION_FIELD_NUMBER
                    )
                    assertMatchesMask(history, fieldMask)
                }

                @Test
                fun `enum fields`() {
                    val policy = HospitalPolicy.newBuilder()
                        .addAcceptedCondition(PatientCondition.CRITICAL)
                        .addAcceptedCondition(PatientCondition.CRITICAL_BUT_STABLE)
                        .build()
                    val fieldMask = fromFieldNumbers<HospitalPolicy>(
                        ACCEPTED_CONDITION_FIELD_NUMBER
                    )
                    assertMatchesMask(policy, fieldMask)
                }
            }

            @Nested
            inner class `not match absent` {

                @Test
                fun `primitive fields`() {
                    val emptyPrescription = Prescription.getDefaultInstance()
                    val fieldMask = fromFieldNumbers<Prescription>(
                        PRESCRIBED_DRUG_FIELD_NUMBER
                    )
                    assertThrows<AssertionError> {
                        assertMatchesMask(emptyPrescription, fieldMask)
                    }
                }

                @Test
                fun `message fields`() {
                    val history = prescriptionHistory {
                        prescriptionReceiver = patientId
                    }
                    val fieldMask = fromFieldNumbers<PrescriptionHistory>(
                        PRESCRIPTION_RECEIVER_FIELD_NUMBER,
                        RECEIVED_PRESCRIPTION_FIELD_NUMBER
                    )
                    assertThrows<AssertionError> {
                        assertMatchesMask(history, fieldMask)
                    }
                }

                @Test
                fun `enum fields`() {
                    val emptyPolicy = HospitalPolicy.getDefaultInstance()
                    val fieldMask = fromFieldNumbers<HospitalPolicy>(
                        ACCEPTED_CONDITION_FIELD_NUMBER
                    )
                    assertThrows<AssertionError> {
                        assertMatchesMask(emptyPolicy, fieldMask)
                    }
                }
            }

            private fun prescribeFromCold(): Prescription {
                val currentTime = Instant.now().toEpochMilli()
                val now = timestamp { seconds = currentTime }
                return prescription {
                    prescribedOn = now
                    prescribedDrug.apply {
                        add("Paracetamol")
                        add("Aspirin")
                        add("Tylenol")
                    }
                }
            }

            private fun newPatient(): PatientId = patientId { UUID.randomUUID().toString() }
        }
    }

    @Nested
    internal inner class `assert values in delta when` {

        @Test
        fun `values are equal`() {
            val expectedValue: Long = value
            assertInDelta(expectedValue, expectedValue, 0)
        }

        @Test
        fun `values are close`() {
            val expectedValue: Long = value
            val actualValue: Long = value
            assertInDelta(expectedValue, actualValue, DELTA)
        }

        @Nested
        inner class `the actual value is` {

            @Test
            fun `less than the sum of the expected value and delta`() {
                val expectedValue: Long = value
                val actualValue = expectedValue + DELTA - 1
                assertInDelta(expectedValue, actualValue, DELTA)
            }

            @Test
            fun `equal to the sum of the expected value and delta`() {
                val expectedValue: Long = value
                val actualValue = expectedValue + DELTA
                assertInDelta(expectedValue, actualValue, DELTA)
            }

            @Test
            fun `greater than the subtraction of the expected value and delta`() {
                val actualValue: Long = value
                val expectedValue = actualValue + DELTA - 1
                assertInDelta(expectedValue, actualValue, DELTA)
            }

            @Test
            fun `equal to the subtraction of the expected value and delta`() {
                val actualValue: Long = value
                val expectedValue = actualValue + DELTA
                assertInDelta(expectedValue, actualValue, DELTA)
            }
        }

        @Nested
        inner class `throwing when the actual value is` {

            @Test
            fun `greater than the sum of the expected value and delta`() {
                val expectedValue: Long = value
                val actualValue = expectedValue + DELTA + 1
                assertThrows<AssertionError> {
                    assertInDelta(expectedValue, actualValue, DELTA)
                }
            }

            @Test
            fun `less than the subtraction of the expected value and delta`() {
                val actualValue: Long = value
                val expectedValue = actualValue + Companion.DELTA + 1
                assertThrows<AssertionError> {
                    assertInDelta(expectedValue, actualValue, Companion.DELTA)
                }
            }
        }
    }

    companion object {
        private const val DELTA: Long = 10
        private const val value: Long = 100
    }
}
