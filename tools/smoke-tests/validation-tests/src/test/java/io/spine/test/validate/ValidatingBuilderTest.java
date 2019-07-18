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

package io.spine.test.validate;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.spine.base.Identifier;
import io.spine.logging.Logging;
import io.spine.test.validate.msg.builder.ArtificialBlizzardVBuilder;
import io.spine.test.validate.msg.builder.Attachment;
import io.spine.test.validate.msg.builder.BlizzardVBuilder;
import io.spine.test.validate.msg.builder.Drink.Ingredient;
import io.spine.test.validate.msg.builder.DrinkCoffeeVBuilder;
import io.spine.test.validate.msg.builder.DrinkTeaVBuilder;
import io.spine.test.validate.msg.builder.DrinkVBuilder;
import io.spine.test.validate.msg.builder.EditTaskStateVBuilder;
import io.spine.test.validate.msg.builder.EssayVBuilder;
import io.spine.test.validate.msg.builder.FrostyWeatherButInWholeNumberVBuilder;
import io.spine.test.validate.msg.builder.FrostyWeatherVBuilder;
import io.spine.test.validate.msg.builder.InconsistentBoundariesVBuilder;
import io.spine.test.validate.msg.builder.Member;
import io.spine.test.validate.msg.builder.Menu;
import io.spine.test.validate.msg.builder.MenuVBuilder;
import io.spine.test.validate.msg.builder.MinorCitizenVBuilder;
import io.spine.test.validate.msg.builder.ProjectVBuilder;
import io.spine.test.validate.msg.builder.RequiredBooleanFieldVBuilder;
import io.spine.test.validate.msg.builder.SafeBet;
import io.spine.test.validate.msg.builder.SafeBetVBuilder;
import io.spine.test.validate.msg.builder.Snowflake;
import io.spine.test.validate.msg.builder.SpacedOutBoundariesVBuilder;
import io.spine.test.validate.msg.builder.Task;
import io.spine.test.validate.msg.builder.TaskVBuilder;
import io.spine.test.validate.msg.builder.UnopenedVBuilder;
import io.spine.test.validate.msg.builder.UnsafeBetVBuilder;
import io.spine.test.validate.msg.builder.UpToInfinityVBuilder;
import io.spine.validate.AbstractValidatingBuilder;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.ValidatingBuilder;
import io.spine.validate.ValidationException;
import io.spine.validate.option.Required;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static com.google.protobuf.ByteString.copyFrom;
import static com.google.protobuf.util.Durations.fromSeconds;
import static com.google.protobuf.util.Timestamps.add;
import static io.spine.base.Identifier.newUuid;
import static io.spine.base.Time.currentTime;
import static io.spine.test.validate.msg.builder.Drink.BeverageCase.TEA;
import static io.spine.test.validate.msg.builder.Drink.Coffee.MilkCase.ALMOND;
import static io.spine.test.validate.msg.builder.Drink.Coffee.MilkCase.DIARY;
import static io.spine.test.validate.msg.builder.Drink.Tea.SweetenerCase.HONEY;
import static io.spine.test.validate.msg.builder.Menu.CriterionCase.VEGETARIAN;
import static io.spine.test.validate.msg.builder.TaskLabel.CRITICAL;
import static io.spine.test.validate.msg.builder.TaskLabel.IMPORTANT;
import static io.spine.test.validate.msg.builder.TaskLabel.OF_LITTLE_IMPORTANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A test suite covering the {@link ValidatingBuilder} behavior.
 *
 * <p>Since most {@code ValidatingBuilders} are generated, the concrete test suits for each of them
 * are not required.
 *
 * <p>Any {@code ValidatingBuilder} implementation should pass these tests. When implementing your
 * own {@code ValidatingBuilder}, be sure to check if it fits the constraints stated below.
 */
@DisplayName("ValidatingBuilder should")
class ValidatingBuilderTest {

    private ProjectVBuilder builder;

    @BeforeEach
    void setUp() {
        builder = fill();
    }

    @Test
    @DisplayName("check required validated repeated fields")
    void checkRequiredValidatedRepeatedFields() {
        assertThrows(ValidationException.class,
                     () -> builder.addTask(Task.getDefaultInstance()));
    }

    @Test
    @DisplayName("ensure required validated repeated fields")
    void ensureRequiredValidatedRepeatedFields() {
        builder.clearTask();
        assertThrows(ValidationException.class,
                     () -> builder.build());
    }

    @Test
    @DisplayName("check required validated map field values")
    void checkRequiredValidatedMapFieldValues() {
        assertThrows(ValidationException.class,
                     () -> builder.putRole("Co-owner", Member.getDefaultInstance()));
    }

    @Test
    @DisplayName("ensure required validated map fields")
    void ensureRequiredValidatedMapFields() {
        builder.clearRole();
        assertThrows(ValidationException.class, () -> builder.build());
    }

    @Test
    @DisplayName("check validated repeated fields")
    void checkValidatedRepeatedFields() {
        assertThrows(ValidationException.class, () -> builder.addSubscriberEmail(""));
    }

    @Test
    @DisplayName("dispense with validated repeated fields")
    void dispenseWithValidatedRepeatedFields() {
        builder.clearSubscriberEmail();
        builder.build();
    }

    @Test
    @DisplayName("check validated map field values")
    void checkValidatedMapFieldValues() {
        assertThrows(ValidationException.class,
                     () -> builder.putAttachment(newUuid(), Attachment.getDefaultInstance()));
    }

    @Test
    @DisplayName("dispense with validated map fields")
    void dispenseWithValidatedMapFields() {
        builder.clearAttachment();
        builder.build();
    }

    @Test
    @DisplayName("accept any required repeated fields")
    void acceptAnyRequiredRepeatedFields() {
        builder.addMember(Member.getDefaultInstance());
    }

    @Test
    @DisplayName("ensure required repeated fields")
    void ensureRequiredRepeatedFields() {
        builder.clearMember();
        assertThrows(ValidationException.class, () -> builder.build());
    }

    @Test
    @DisplayName("accept any required map field value")
    void acceptAnyRequiredMapFieldValue() {
        builder.putDeletedTask(newUuid(), timeInFuture());
    }

    @Test
    @DisplayName("ensure required map fields")
    void ensureRequiredMapFields() {
        builder.clearDeletedTask();
        assertThrows(ValidationException.class, () -> builder.build());
    }

    @Test
    @DisplayName("accept any unchecked repeated fields")
    void acceptAnyUncheckedRepeatedFields() {
        builder.addDescription("");
    }

    @Test
    @DisplayName("dispense with unchecked repeated fields")
    void dispenseWithUncheckedRepeatedFields() {
        builder.clearDescription();
        builder.build();
    }

    @Test
    @DisplayName("accept any unchecked map field value")
    void acceptAnyUncheckedMapFieldValue() {
        builder.putLabel("empty", "none");
    }

    @Test
    @DisplayName("dispense with unchecked map fields")
    void dispenseWithUncheckedMapFields() {
        builder.clearLabel();
        builder.build();
    }

    @Test
    @DisplayName("not allow to change the value of a (set_once) message field")
    void testMessageFieldMutations() {
        Stream<Function<TaskVBuilder, ?>> enumFieldMutations = Stream.of(
                taskVBuilder -> taskVBuilder.setLabel(CRITICAL),
                taskVBuilder -> taskVBuilder.mergeFrom(sampleTask()),
                TaskVBuilder::clearLabel
        );
        testSetOnce(taskVBuilder -> taskVBuilder.setLabel(IMPORTANT), enumFieldMutations);
    }

    @Test
    @DisplayName("not allow to change the value of a (set_once) string field")
    void testStringFieldMutations() {
        Stream<Function<TaskVBuilder, ?>> stringFieldMutations = Stream.of(
                taskVBuilder -> taskVBuilder.setId(newUuid()),
                taskVBuilder -> taskVBuilder.mergeFrom(sampleTask()),
                TaskVBuilder::clearId
        );
        testSetOnce(taskVBuilder -> taskVBuilder.setId(newUuid()), stringFieldMutations);
    }

    @Test
    @DisplayName("not allow to change the value of a (set_once) enum field")
    void testEnumFieldMutations() {
        Stream<Function<TaskVBuilder, ?>> messageFieldMutations = Stream.of(
                taskVBuilder -> taskVBuilder.setAssignee(Member.getDefaultInstance()),
                taskVBuilder -> taskVBuilder.mergeFrom(sampleTask()),
                TaskVBuilder::clearAssignee
        );
        testSetOnce(taskVBuilder -> taskVBuilder.setAssignee(member()), messageFieldMutations);
    }

    private static void testSetOnce(Function<TaskVBuilder, TaskVBuilder> setup,
                                    Stream<Function<TaskVBuilder, ?>> mutateOperations) {
        mutateOperations.forEach(
                operation -> testOption(TaskVBuilder.newBuilder(), setup, operation)
        );
    }

    /**
     * Tests a validating builder that validates a field option by prohibiting some sort
     * of mutation.
     *
     * <p>First, applies the specified setup operation and then applies the specified rule-violating
     * operation and checks, whether an exception was thrown.
     *
     * @param builder
     *         an initial value of a builder
     * @param builderSetup
     *         first mutation of a {@code set_once field}
     * @param violatingOperation
     *         an operation that is supposed to be illegal
     * @param <E>
     *         type of the builder
     */
    private static <E extends AbstractValidatingBuilder<?, ?>>
    void testOption(E builder, Function<E, E> builderSetup, Function<E, ?> violatingOperation) {
        E initialBuilder = builderSetup.apply(builder);
        assertThrows(ValidationException.class, () -> violatingOperation.apply(initialBuilder));
    }

    @Test
    @DisplayName("not allow to change a state of an implicitly `(set_once)` field")
    void testSetOnceImplicitForEntities() {
        testOption(EditTaskStateVBuilder.newBuilder(),
                   builder -> builder.setEditId(newUuid()),
                   builder -> builder.setEditId(newUuid())
                                     .build());
    }

    @DisplayName("not allow to add duplicate entries to a `(onDuplicate) = ERROR` marked field")
    @Test
    void testDistinctThrows() {
        testOption(BlizzardVBuilder.newBuilder(),
                   builder -> builder.addSnowflake(triangularSnowflake()),
                   builder -> builder.addSnowflake(triangularSnowflake())
                                     .build());
    }

    @DisplayName("not allow to `allAdd` duplicate entries to a `(onDuplicate) = ERROR` marked field")
    @Test
    void testDistinctThrowsOnAddAll() {
        List<Snowflake> snowflakes = ImmutableList.of(triangularSnowflake(), triangularSnowflake());
        testOption(BlizzardVBuilder.newBuilder(),
                   builder -> builder.addSnowflake(triangularSnowflake()),
                   builder -> builder.addAllSnowflake(snowflakes)
                                     .build());
    }

    @DisplayName("ignore duplicates in a `(on_duplicate) = IGNORE` marked field")
    @Test
    void testDistinctIgnoresIfRequested() {
        ArtificialBlizzardVBuilder builder = ArtificialBlizzardVBuilder.newBuilder();
        builder.addSnowflake(triangularSnowflake())
               .addSnowflake(triangularSnowflake());
    }

    @DisplayName("not allow values that don't fit into a closed range")
    @Test
    void testDoesNotAllowBelow() {
        double unsafeOdds = safeOdds() - 0.1d;
        testOption(SafeBetVBuilder.newBuilder(),
                   builder -> builder,
                   builder -> builder.setOdds(unsafeOdds));
    }

    @DisplayName("allow values that are equal to the lower endpoint of an unclosed range")
    @Test
    void testFitsRightIntoAnOpenedRange() {
        SafeBet safeBet = SafeBetVBuilder
                .newBuilder()
                .setOdds(safeOdds())
                .build();
        assertEquals(safeOdds(), safeBet.getOdds());
    }

    @DisplayName("disallow ranges with incorrect types")
    @Test
    void testInvalidRangeTypes() {
        MinorCitizenVBuilder builder = MinorCitizenVBuilder.newBuilder();
        assertThrows(IllegalStateException.class, () -> builder.setAge(18));

    }

    @DisplayName("disallow values that are equal to the upper endpoint of an open range")
    @Test
    void testDoesNotFitRightIntoAnOpenRange() {
        testOption(UnsafeBetVBuilder.newBuilder(),
                   builder -> builder,
                   builder -> builder.setOdds(safeOdds()));
    }

    @DisplayName("throw an exception upon finding a malformed range")
    @Test
    void throwsOnMalformedRangeNoLeftBorder() {
        UnopenedVBuilder builder = UnopenedVBuilder.newBuilder();
        assertThrows(IllegalArgumentException.class, () -> builder.setValue(0));
    }

    @DisplayName("throw an exception upon finding a malformed range")
    @Test
    void throwsOnMalformedRangeNoRightBorder() {
        UpToInfinityVBuilder builder = UpToInfinityVBuilder.newBuilder();
        assertThrows(IllegalArgumentException.class, () -> builder.setValue(0));
    }

    @DisplayName("correctly parse ranges with negative values")
    @Test
    void worksWithNegativeRanges() {
        FrostyWeatherVBuilder weatherBuilder = FrostyWeatherVBuilder.newBuilder();
        weatherBuilder.setCelcius(-6.5d);
        testOption(FrostyWeatherVBuilder.newBuilder(),
                   builder -> builder,
                   builder -> builder.setCelcius(-10.0d));
    }

    @DisplayName("throw on boundaries with inconsistent types")
    @Test
    void throwOnInconsistentBoundaryTypes() {
        InconsistentBoundariesVBuilder builder = InconsistentBoundariesVBuilder.newBuilder();
        assertThrows(IllegalStateException.class, () -> builder.setValue(3.2d));
    }

    @DisplayName("throw on boundaries that are inconsistent with the constrained value type")
    @Test
    void throwOnIncosistentBoundaryAndValueTypes() {
        FrostyWeatherButInWholeNumberVBuilder builder = FrostyWeatherButInWholeNumberVBuilder
                .newBuilder();
        assertThrows(IllegalStateException.class, () -> builder.setCelcius(-5.0d));
    }

    @DisplayName("produce correct error messages on numbers that don't fit the ranges")
    @Test
    void testCorrectErrorMessageDoesNotFitTheRange() {
        String expectedMessageFormat =
                "Number must be greater than %s and less than or equal to %s.";
        FrostyWeatherVBuilder weatherBuilder = FrostyWeatherVBuilder.newBuilder();
        try {
            weatherBuilder.setCelcius(30.0d);
        } catch (ValidationException e) {
            List<ConstraintViolation> violations = e.getConstraintViolations();
            assertEquals(1, violations.size());
            ConstraintViolation violation = violations.get(0);
            String actualFormat = violation.getMsgFormat();
            assertEquals(expectedMessageFormat, actualFormat);
        }
    }

    @DisplayName("not get affected by ranges with spaces")
    @Test
    void testCorrectRangesWithSpaces() {
        testOption(SpacedOutBoundariesVBuilder.newBuilder(),
                   builder -> builder,
                   builder -> builder.setValue(32));
    }

    @Test
    @DisplayName("obtain the case a oneof")
    void provideOneofCase() {
        Menu.CriterionCase criterionCase = MenuVBuilder
                .newBuilder()
                .setVegetarian(true)
                .getCriterionCase();
        assertThat(criterionCase).isEqualTo(VEGETARIAN);
    }

    @Test
    @DisplayName("generate case getters for nested oneofs")
    void generateComplexOneofAccessors() {
        Ingredient honey = Ingredient
                .newBuilder()
                .setAmountGrams(2)
                .build();
        DrinkTeaVBuilder tea = DrinkTeaVBuilder
                .newBuilder()
                .setHoney(honey);
        DrinkVBuilder drink = DrinkVBuilder
                .newBuilder()
                .setTea(tea.build());
        assertThat(tea.getSweetenerCase()).isEqualTo(HONEY);
        assertThat(drink.getBeverageCase()).isEqualTo(TEA);
    }

    @Test
    @DisplayName("update the value of oneof case when set many times")
    void updateOneofCase() {
        DrinkCoffeeVBuilder builder = DrinkCoffeeVBuilder
                .newBuilder()
                .setDiary(Ingredient.getDefaultInstance());
        assertThat(builder.getMilkCase()).isEqualTo(DIARY);
        builder.setAlmond(Ingredient.getDefaultInstance());
        assertThat(builder.getMilkCase()).isEqualTo(ALMOND);
    }

    private static Snowflake triangularSnowflake() {
        return Snowflake
                .newBuilder()
                .setEdges(3)
                .build();
    }

    /**
     * Creates a valid {@link ProjectVBuilder} instance.
     */
    private static ProjectVBuilder fill() {
        ProjectVBuilder builder = ProjectVBuilder
                .newBuilder()
                .addTask(task())
                .addMember(member())
                .putRole("Owner", member())
                .putDeletedTask(newUuid(), timeInPast());
        builder.build(); // Ensure no ValidationException is thrown.
        return builder;
    }

    private static Timestamp timeInPast() {
        return add(currentTime(), fromSeconds(-1000L));
    }

    private static Timestamp timeInFuture() {
        return add(currentTime(), fromSeconds(1000L));
    }

    private static double safeOdds() {
        return 0.5d;
    }

    private static Task task() {
        String id = newUuid();
        String name = "Task name" + id;
        return Task.newBuilder()
                   .setId(id)
                   .setName(name)
                   .build();
    }

    private static Task sampleTask() {
        Member member = Member.getDefaultInstance();
        return Task.newBuilder()
                   .setId(Identifier.newUuid())
                   .setAssignee(member)
                   .setLabel(OF_LITTLE_IMPORTANCE)
                   .setName("Do something unimportant")
                   .build();
    }

    private static Member member() {
        ByteString bytes = copyFrom(new byte[]{(byte) 1, (byte) 2, (byte) 3});
        Member.Builder member = Member
                .newBuilder()
                .setId(newUuid())
                .setName("John Smith")
                .setAvatarImage(bytes);
        return member.build();
    }
}
