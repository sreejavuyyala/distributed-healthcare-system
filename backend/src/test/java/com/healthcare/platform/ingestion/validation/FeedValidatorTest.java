package com.healthcare.platform.ingestion.validation;

import com.healthcare.platform.ingestion.FeedName;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Phase 27-equivalent: schema validation, null handling, invalid dates, duplicate natural keys within a batch. */
class FeedValidatorTest {

    private final FeedValidator validator = new FeedValidator();

    @Test
    void validRowHasNoErrors() throws IOException {
        CSVRecord record = parseOne(
                "patient_id,first_name,last_name,date_of_birth,gender,zip_code",
                "PAT-0000001,Jane,Doe,1990-01-01,Female,90210");

        List<String> errors = validator.validate(FeedName.PATIENTS, record, new HashSet<>());

        assertThat(errors).isEmpty();
    }

    @Test
    void missingRequiredFieldIsRejected() throws IOException {
        CSVRecord record = parseOne(
                "patient_id,first_name,last_name,date_of_birth,gender,zip_code",
                "PAT-0000001,,Doe,1990-01-01,Female,90210");

        List<String> errors = validator.validate(FeedName.PATIENTS, record, new HashSet<>());

        assertThat(errors).anyMatch(e -> e.contains("first_name"));
    }

    @Test
    void invalidDateIsRejected() throws IOException {
        CSVRecord record = parseOne(
                "patient_id,first_name,last_name,date_of_birth,gender,zip_code",
                "PAT-0000001,Jane,Doe,not-a-date,Female,90210");

        List<String> errors = validator.validate(FeedName.PATIENTS, record, new HashSet<>());

        assertThat(errors).anyMatch(e -> e.contains("date_of_birth"));
    }

    @Test
    void duplicateNaturalKeyWithinBatchIsRejected() throws IOException {
        Set<String> seenKeys = new HashSet<>();
        CSVRecord first = parseOne(
                "patient_id,first_name,last_name,date_of_birth,gender,zip_code",
                "PAT-0000001,Jane,Doe,1990-01-01,Female,90210");
        CSVRecord duplicate = parseOne(
                "patient_id,first_name,last_name,date_of_birth,gender,zip_code",
                "PAT-0000001,Jane,Doe,1990-01-01,Female,90210");

        List<String> firstErrors = validator.validate(FeedName.PATIENTS, first, seenKeys);
        List<String> duplicateErrors = validator.validate(FeedName.PATIENTS, duplicate, seenKeys);

        assertThat(firstErrors).isEmpty();
        assertThat(duplicateErrors).anyMatch(e -> e.contains("duplicate natural key"));
    }

    @Test
    void optionalBlankFieldIsAllowed() throws IOException {
        CSVRecord record = parseOne(
                "patient_id,first_name,last_name,date_of_birth,gender,zip_code",
                "PAT-0000001,Jane,Doe,1990-01-01,,");

        List<String> errors = validator.validate(FeedName.PATIENTS, record, new HashSet<>());

        assertThat(errors).isEmpty();
    }

    private CSVRecord parseOne(String header, String dataRow) throws IOException {
        try (CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
                .parse(new StringReader(header + "\n" + dataRow + "\n"))) {
            return parser.getRecords().get(0);
        }
    }
}