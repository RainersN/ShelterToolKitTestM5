package lv.bootcamp.shelter.task23;

import lv.bootcamp.shelter.model.Animal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tasks 2 & 3: Parameterized tests and exception tests
 *
 * Practice:
 * - @ParameterizedTest with @CsvSource
 * - @ValueSource and @NullAndEmptySource
 * - assertThrows with message checks
 * - AssertJ assertThatThrownBy
 *
 * Instructions:
 * Write tests for AnimalValidator. Each TODO describes one test to write.
 */
@DisplayName("AnimalValidator")
class AnimalValidatorTest {

    private AnimalValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AnimalValidator();
    }

    // ==================== Task 2: Parameterized tests ====================

    @Nested
    @DisplayName("validateName")
    class ValidateName {

        @ParameterizedTest
        @ValueSource(strings = {"Buddy", "Luna", "Mr. Whiskers", "X"})
        @DisplayName("accepts valid names")
        void shouldAcceptValidNames(String name) {
            assertDoesNotThrow(() -> validator.validateName(name));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("rejects blank or null names")
        void shouldRejectBlankNames(String name) {

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                validator.validateName(name);
            });
            //tests if contains "must not be blank" in message
            assertTrue(exception.getMessage().contains("must not be blank"));
        }

        @Test
        @DisplayName("rejects name longer than 100 characters")
        void shouldRejectOverlyLongName() {
            //hard-coded string,generated with online generator. length is 101 characters
            String longName = "yxHmvmbaWfBsqdvNkOOZbiVMcxdr69hpImcLoT8twnKnS7jOsnVnYJaJOdJxqAz7MlelTDx7wTjWkpjizDx8FL1W6SZBVeRobLcHF";

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                validator.validateName(longName);
            });
            //tests if contains "100 characters" in message
            assertTrue(exception.getMessage().contains("100 characters"));
        }
    }

    @Nested
    @DisplayName("validateAge")
    class ValidateAge {

        @ParameterizedTest
        @CsvSource({
                "0",
                "1",
                "10",
                "50"
        })
        @DisplayName("accepts valid ages")
        void shouldAcceptValidAges(int age) {
            assertDoesNotThrow(() -> validator.validateAge(age));
        }

        @ParameterizedTest
        @CsvSource({
                "-1, must not be negative",
                "-100, must not be negative",
                "51, seems unrealistic",
                "999, seems unrealistic"
        })
        @DisplayName("rejects invalid ages with correct message")
        void shouldRejectInvalidAges(int age, String expectedMessagePart) {

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                validator.validateAge(age);
            });
            //tests if contains expected message part
            assertTrue(exception.getMessage().contains(expectedMessagePart));
        }
    }

    // ==================== Task 3: Exception tests ====================

    @Nested
    @DisplayName("validate (full animal)")
    class ValidateFullAnimal {

        @Test
        @DisplayName("throws NullPointerException for null animal")
        void shouldThrowForNullAnimal() {
            Exception exception = assertThrows(NullPointerException.class, () -> {
                validator.validate(null);
            });
            //tests if message contains "must not be null"
            assertTrue(exception.getMessage().contains("must not be null"));
        }

        @Test
        @DisplayName("throws for animal with blank name")
        void shouldThrowForBlankName() {
            Animal animal = new Animal("","Cat",3,false,LocalDate.of(2025, 11, 20));
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                validator.validate(animal);
            });
            //tests if message contains "must not be blank",since validate() calls validateName(),and the exception is related to being blank
            assertTrue(exception.getMessage().contains("must not be blank"));
        }

        @Test
        @DisplayName("throws for animal with blank species")
        void shouldThrowForBlankSpecies() {
            Animal animal = new Animal("Mittens","",3,false,LocalDate.of(2025, 11, 20));
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                validator.validate(animal);
            });
            //tests if message contains "must not be blank",since validate() calls validateSpecies()
            assertTrue(exception.getMessage().contains("must not be blank"));
        }

        @Test
        @DisplayName("throws for animal with negative age")
        void shouldThrowForNegativeAge() {

            Animal animal = new Animal("Mittens","Cat",-3,false,LocalDate.of(2025, 11, 20));
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                validator.validate(animal);
            });
            //tests if message contains "must not be negative",since validate() calls validateAge(),and the issue is the negative age
            assertTrue(exception.getMessage().contains("must not be negative"));

        }

        @Test
        @DisplayName("does not throw for fully valid animal")
        void shouldPassForValidAnimal() {
            Animal animal = new Animal("Mittens","Cat",3,false,LocalDate.of(2025, 11, 20));

            assertDoesNotThrow(() -> validator.validate(animal));
        }
    }
}
