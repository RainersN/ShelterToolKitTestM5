package lv.bootcamp.shelter.task5;

import lv.bootcamp.shelter.model.Animal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Task 5: Nested test classes for CSV parsing
 * <p>
 * Practice:
 * - @Nested to organize by scenario
 * - @DisplayName for readable output
 * - Testing Optional results (isPresent, isEmpty)
 * - Testing file I/O with temp files
 * <p>
 * Instructions:
 * Write tests for AnimalCsvParser. Use @Nested classes to group tests by scenario.
 * For file-based tests, use Files.createTempFile() and Files.writeString() to create test data.
 */
@DisplayName("AnimalCsvParser")
class AnimalCsvParserTest {

    private AnimalCsvParser parser;

    @BeforeEach
    void setUp() {
        parser = new AnimalCsvParser();
    }

    // ==================== parseRow tests ====================

    @Nested
    @DisplayName("When parsing valid rows")
    class ValidRows {

        @Test
        @DisplayName("parses a complete row into an Animal")
        void shouldParseCompleteRow() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3,true,2026-01-15");
            Animal animal = result.get();

            assertThat(result).isPresent();
            assertEquals("Buddy", animal.getName());
            assertEquals("Dog", animal.getSpecies());
            assertEquals(3, animal.getAge());
            assertTrue(animal.isVaccinated());
            assertEquals(LocalDate.of(2026, 1, 15), animal.getIntakeDate());
        }

        @Test
        @DisplayName("trims whitespace from fields")
        void shouldTrimWhitespace() {
            Optional<Animal> result = parser.parseRow("  Buddy , Dog , 3 , true , 2026-01-15 ");
            Animal animal = result.get();

            assertEquals("Buddy", animal.getName());
        }

        @Test
        @DisplayName("parses vaccinated=false correctly")
        void shouldParseFalseVaccination() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3,false,2026-01-15");
            Animal animal = result.get();

            assertFalse(animal.isVaccinated());
        }
    }

    @Nested
    @DisplayName("When parsing malformed rows")
    class MalformedRows {

        @Test
        @DisplayName("returns empty for null input")
        void shouldReturnEmptyForNull() {
            Optional<Animal> result = parser.parseRow(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty for blank input")
        void shouldReturnEmptyForBlank() {

            Optional<Animal> result = parser.parseRow("   ");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when row has fewer than 5 fields")
        void shouldReturnEmptyForTooFewFields() {

            Optional<Animal> result = parser.parseRow("Buddy,Dog,3");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when name is missing")
        void shouldReturnEmptyForMissingName() {
            Optional<Animal> result = parser.parseRow(",Dog,3,true,2026-01-15");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when age is not a number")
        void shouldReturnEmptyForBadAge() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,old,true,2026-01-15");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when age is negative")
        void shouldReturnEmptyForNegativeAge() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,-1,true,2026-01-15");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when date is invalid")
        void shouldReturnEmptyForBadDate() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3,true,not-a-date");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("When handling edge cases")
    class EdgeCases {

        @Test
        @DisplayName("handles vaccinated field as any non-true string → false")
        void shouldTreatNonTrueAsFalse() {

            Optional<Animal> result = parser.parseRow("Buddy,Dog,3,maybe,2026-01-15");
            Animal animal = result.get();

            assertFalse(animal.isVaccinated());
        }

        @Test
        @DisplayName("handles age 0 as valid")
        void shouldAcceptAgeZero() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,0,true,2026-01-15");
            Animal animal = result.get();

            assertThat(result).isPresent();
            assertEquals(0, animal.getAge());
        }
    }

    // ==================== parseFile tests ====================

    @Nested
    @DisplayName("When parsing a CSV file")
    class ParseFile {

        @Test
        @DisplayName("parses valid rows and counts skipped rows")
        void parsesValidRowsAndSkipsMalformed() throws IOException {
            String content = """
                name,species,age,vaccinated,intakeDate
                Buddy,Dog,3,true,2026-01-15
                Whiskers,Cat,5,false,2026-02-20
                Rex,Dog,2,true,2026-03-10
                Broken,Cat,not-a-number,true,2026-04-01
                """;

            Path tempFile = Files.createTempFile("test-intake", ".csv");
            try {
                Files.writeString(tempFile, content, StandardCharsets.UTF_8);

                AnimalCsvParser.ParseResult result = parser.parseFile(tempFile);

                assertEquals(3, result.animals().size());
                assertEquals(1, result.skippedRows());
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("returns empty result for file with only a header")
        void shouldReturnEmptyForHeaderOnly() throws IOException {

            String content = "name,species,age,vaccinated,intakeDate";

            Path tempFile = Files.createTempFile("test-intake", ".csv");
            try {
                Files.writeString(tempFile, content, StandardCharsets.UTF_8);

                AnimalCsvParser.ParseResult result = parser.parseFile(tempFile);

                assertThat(result.animals()).isEmpty();
                assertEquals(0, result.skippedRows());
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("throws IOException for non-existent file")
        void shouldThrowForMissingFile() {
            assertThrows(IOException.class, () -> {
                parser.parseFile(Path.of("does-not-exist.csv"));
            });
        }
    }
}
