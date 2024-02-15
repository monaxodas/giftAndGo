package com.giftandgo.assessment.assessment.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.giftandgo.assessment.configuration.FileProcessingConfiguration;
import com.giftandgo.assessment.exception.RestrictedCountryException;
import com.giftandgo.assessment.exception.RestrictedISPException;
import com.giftandgo.assessment.exception.ValidationError;
import com.giftandgo.assessment.model.InputFileEntry;
import com.giftandgo.assessment.service.ValidationService;
import com.giftandgo.assessment.service.impl.ValidationServiceImpl;

public class ValidationServiceTest {

    private ValidationService subject;
    private FileProcessingConfiguration configuration;

    private List<String> invalidCountries = List.of("USA", "Spain");
    private List<String> invalidIsps = List.of("AWS", "AZURE");

    private String validUUID = UUID.randomUUID().toString();
    private String validId = "1X1D14";
    private String validName = "John Snow";
    private String validTransPort = "Bike";
    private String validLikes = "Turtles";
    private String validAvgSpeed = "13.1";
    private String validTopSpeed = "134.1";

    @BeforeEach
    void setup() {
        configuration = new FileProcessingConfiguration();
        configuration.setIpDataCentersBlockList(invalidIsps);
        configuration.setIpCountriesBlockList(invalidCountries);
        configuration.setValidationEnabled(true);
        subject = new ValidationServiceImpl(configuration);
    }

    @Test
    void testHappyPath() {
        final var validCountry = "Greece";
        final var validIsp = "Virgin";
        subject.validateCountryAndIsp(validCountry, validIsp);
    }

    @Test
    void testInvalidCountry() {
        final var invalidCountry = "USA";
        final var validIsp = "Virgin";
        assertThrows(RestrictedCountryException.class, () -> subject.validateCountryAndIsp(invalidCountry, validIsp));
    }

    @Test
    void testInvalidISP() {
        final var validCountry = "Greece";
        final var invalidIsp = "AWS";
        assertThrows(RestrictedISPException.class, () -> subject.validateCountryAndIsp(validCountry, invalidIsp));
    }

    @Test
    void testInvalidISPAndCountry() {
        final var invalidCountry = "Spain";
        final var invalidIsp = "AWS";
        assertThrows(RestrictedCountryException.class, () -> subject.validateCountryAndIsp(invalidCountry, invalidIsp));
    }

    @Test
    void testFileLine_lessColumns() {
        final var line = combineColumnsWithSeparator(validUUID, validId, validName, validLikes, validTransPort, validAvgSpeed);
        assertThrows(ValidationError.class, () -> subject.validateInputFileContent(List.of(line)));
    }

    @Test
    void testFileLine_moreColumns() {
        final var line = combineColumnsWithSeparator(validUUID, validId, validName, validLikes, validTransPort, validAvgSpeed, validTopSpeed, "bee");
        assertThrows(ValidationError.class, () -> subject.validateInputFileContent(List.of(createValidLine(), line)));
    }

    @ParameterizedTest
    @CsvSource({
        "a6c972a0-5430-4566-a966-670de1518q,1X1D14,Michael Jackson, Likes turtles,Rides a car, 50, 100.1",
        "a6c972a0-5430-4566-a966-670de15185c1,1X1D14!,Michael Jackson, Likes turtles,Rides a car, 50, 100.1",
        "a6c972a0-5430-4566-a966-670de15185c1,1X1D14,Michael! Jackson, Likes turtles,Rides a car, 50, 100.1",
        "a6c972a0-5430-4566-a966-670de15185c1,1X1D14,Michael J123ackson, Likes turtles,Rides a car, 50, 100.1",
        "a6c972a0-5430-4566-a966-670de15185c1,1X1D14,Michael Jackson, Likes3 turtles,Rides a car, 50, 100.1",
        "a6c972a0-5430-4566-a966-670de15185c1,1X1D14,Michael Jackson, Likes turtles,Rid??es a car, 50, 100.1",
        "a6c972a0-5430-4566-a966-670de15185c1,1X1D14,Michael Jackson, Likes turtles,Ride1s a car, 50, 100.1",
        "a6c972a0-5430-4566-a966-670de15185c1,1X1D14,Michael Jackson, Likes turtles,Rides a car, test, 100.1",
        "a6c972a0-5430-4566-a966-670de15185c1,1X1D14,Michael Jackson, Likes turtles,Rides a car, 50, 1233?.1",
        "a6c972a0-5430-4566-a966-670de15185c1,1X1D14,Michael Jackson, Likes turtles,Rides a car, 200, 100.1",
        "a6c972a0-5430-4566-a966-670de15185c1,1X1D14,Michael Jackson, L21ikes turtles,Rides a car, 50, 100.1",
        "a6c972a0-5430-4566-a966-670de15185c1,2X2D241,Mike Smith, Likes turtles  ,Rides a car, 50, 50.1",
        "a6c972a0-5430-4566-a966-670de15185c1,2X2D24,MikeSmith, Likes turtles  ,Rides a car, 50, 50.1",
        "a6c972a0-5430-4566-a966-670de15185c1,2X2D24,Mike Smith,ikes turtles  ,Rides a car, 50, 50.1",
        "a6c972a0-5430-4566-a966-670de15185c1,2X2D24,Mike Smith, Likes turtles  ,ides a car, 50, 50.1",
        "a6c972a0-5430-4566-a966-670de15185c1,2X2D24,Mike Smith, Likes turtles1  ,Rides a car, 50, 50.1",
        "a6c972a0-5430-4566-a966-670de15185c1,2X2D24,Mike Smith, Likes turtles  ,R2ides a car, 50, 50.1",
        "a6c972a0-5430-4566-a966-670de15185c1,2X2D24,Mike Smith, Likes turtles  ,Rides car, 50, 50.1",
    })
    void testFileLine_invalidFields(String uuid, String id, String name, String likes, String transport, String avgSpeed, String topSpeed) {
        final var line = combineColumnsWithSeparator(uuid, id, name, likes, transport, avgSpeed, topSpeed);
        assertThrows(ValidationError.class, () -> subject.validateInputFileContent(List.of(createValidLine(), line)));
    }

    @ParameterizedTest
    @CsvSource({
        "a6c972a0-5430-4566-a966-670de15185c1,1X1D14,Michael Jackson, Likes turtles,Rides a car, 50, 100.1",
        "a6c972a0-5430-4566-a966-670de15185c1  ,2X2D24,Mike Smith, Likes turtles  ,Rides an apple, 50, 50.1",
        "fb7620f4-5ee8-44d4-82ab-57be08452213,3X3D35,Jenny Walters, Likes Avocados ,Rides a car, 99.99, 100",
    })
    void testFileLine_validFields(String uuid, String id, String name, String likes, String transport, String avgSpeed, String topSpeed) {
        final var line = combineColumnsWithSeparator(uuid, id, name, likes, transport, avgSpeed, topSpeed);
        final var inputFileEntries = subject.validateInputFileContent(List.of(line));

        assertThat(inputFileEntries.size()).isEqualTo(1);

        final var inputFileEntry = inputFileEntries.get(0);

        assertThat(inputFileEntry.uuid()).isEqualTo(UUID.fromString(uuid));
        assertThat(inputFileEntry.id()).isEqualTo(id);
        assertThat(inputFileEntry.name()).isEqualTo(name);
        assertThat(inputFileEntry.likes()).isEqualTo(likes);
        assertThat(inputFileEntry.transport()).isEqualTo(transport);
        assertThat(inputFileEntry.avgSpeed()).isEqualTo(Double.parseDouble(avgSpeed));
        assertThat(inputFileEntry.topSpeed()).isEqualTo(Double.parseDouble(topSpeed));
    }

    @Test
    void testInvalidLineWithValidationDisabled() {
        configuration.setValidationEnabled(false);
        final var line = combineColumnsWithSeparator("a6c972a0-5430-4566-a966-670de15185c1",
            "2X2D24",
            "Mike Smith",
            "ikes turtles  ",
            "Rides a car",
            " 50",
            " 50.1");

        final var inputFileEntries = subject.validateInputFileContent(List.of(line));

        assertThat(inputFileEntries.size()).isEqualTo(1);

        final var inputFileEntry = inputFileEntries.get(0);

        assertThat(inputFileEntry.uuid()).isEqualTo(UUID.fromString("a6c972a0-5430-4566-a966-670de15185c1"));
        assertThat(inputFileEntry.id()).isEqualTo("2X2D24");
        assertThat(inputFileEntry.name()).isEqualTo("Mike Smith");
        assertThat(inputFileEntry.likes()).isEqualTo("ikes turtles");
        assertThat(inputFileEntry.transport()).isEqualTo("Rides a car");
        assertThat(inputFileEntry.avgSpeed()).isEqualTo(Double.parseDouble("50"));
        assertThat(inputFileEntry.topSpeed()).isEqualTo(Double.parseDouble("50.1"));
    }

    @Test
    void testInvalidColumnNumberWithValidationDisabled() {
        configuration.setValidationEnabled(false);
        final var line = combineColumnsWithSeparator("a6c972a0-5430-4566-a966-670de15185c1",
            "2X2D24",
            "Mike Smith",
            "ikes turtles  ",
            "Rides a car",
            " 50");

        final var inputFileEntries = subject.validateInputFileContent(List.of(line));
        assertThat(inputFileEntries.size()).isEqualTo(0);
    }

    private String combineColumnsWithSeparator(String... columns) {
        return String.join("|", columns);
    }

    private String createValidLine() {
        return combineColumnsWithSeparator(validUUID, validId, validName, validLikes, validTransPort, validAvgSpeed, validTopSpeed);
    }

}
