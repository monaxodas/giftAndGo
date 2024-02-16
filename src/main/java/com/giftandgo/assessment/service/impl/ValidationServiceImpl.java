package com.giftandgo.assessment.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.giftandgo.assessment.configuration.FileProcessingConfiguration;
import com.giftandgo.assessment.exception.RestrictedCountryException;
import com.giftandgo.assessment.exception.RestrictedISPException;
import com.giftandgo.assessment.exception.ValidationError;
import com.giftandgo.assessment.model.InputFileEntry;
import com.giftandgo.assessment.model.ParsedColumn;
import com.giftandgo.assessment.service.ValidationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ValidationServiceImpl implements ValidationService {
    private final FileProcessingConfiguration fileProcessingConfiguration;

    public ValidationServiceImpl(FileProcessingConfiguration fileProcessingConfiguration) {
        this.fileProcessingConfiguration = fileProcessingConfiguration;
    }

    @Override
    public void validateCountryAndIsp(final String country, final String isp) {
        if (!fileProcessingConfiguration.getValidationEnabled()) {
            log.info("Validation is disabled, proceeding without validating request");
            return;
        }

        Optional.ofNullable(country)
            .filter(c -> fileProcessingConfiguration.getIpCountriesBlockList().contains(c))
            .ifPresent(c -> {
                throw new RestrictedCountryException(c);
            });

        Optional.ofNullable(isp)
            .filter(i -> fileProcessingConfiguration.getIpDataCentersBlockList().contains(i))
            .ifPresent(i -> {
                throw new RestrictedISPException(i);
            });
    }

    @Override
    public List<InputFileEntry> validateInputFileContent(final List<String> fileLines) {
        log.debug("Initiating file validation");
        return IntStream.range(0, fileLines.size())
            .mapToObj(i -> {
                final var line = fileLines.get(i);
                final var split = line.split(fileProcessingConfiguration.getColumnSeparator());
                if (split.length != 7) {
                    if (fileProcessingConfiguration.getValidationEnabled()) {
                        throw new ValidationError("Invalid number of columns for line: " + i);
                    } else {
                        return null;
                    }
                }
                final var uuid = validateUUID(i, split);
                final var id = validateId(i, split);
                final var name = validateName(i, split);
                final var likes = validateLikes(i, split);
                final var transport = validateTransport(i, split);
                final var avgSpeed = validateNumberField(i, split, ParsedColumn.AVG_SPEED);
                final var topSpeed = validateNumberField(i, split, ParsedColumn.TOP_SPEED);

                validateSpeeds(avgSpeed, topSpeed, i);

                return new InputFileEntry(uuid, id, name, likes, transport, avgSpeed, topSpeed);
            })
            .filter(Objects::nonNull)
            .toList();
    }

    private UUID validateUUID(final int lineIndex, final String[] split) {
        final var parsedColumn = ParsedColumn.UUID;
        final var uuid = split[parsedColumn.ordinal()].trim();
        try {
            return UUID.fromString(uuid);
        } catch (final Exception e) {
            if (fileProcessingConfiguration.getValidationEnabled()) {
                throwValidationError(parsedColumn, lineIndex, uuid);
            }
        }
        return null;
    }

    private String validateId(final int lineIndex, final String[] split) {
        final var column = ParsedColumn.ID;
        final var id = split[column.ordinal()].trim();
        if (fileProcessingConfiguration.getValidationEnabled() && !id.matches("^[A-Z`0-9]{6}$")) {
            throwValidationError(column, lineIndex, id);
        }
        return id;
    }

    private String validateName(final int lineIndex, final String[] split) {
        final var column = ParsedColumn.NAME;
        final var name = split[column.ordinal()].trim();
        if (fileProcessingConfiguration.getValidationEnabled() && !name.matches("^[A-Z][a-z]+ [A-Z][a-z]+$")) {
            throwValidationError(column, lineIndex, name);
        }
        return name;
    }

    private String validateLikes(final int lineIndex, final String[] split) {
        final var column = ParsedColumn.LIKES;
        final var likes = split[column.ordinal()].trim();
        if (fileProcessingConfiguration.getValidationEnabled() && !likes.startsWith("Likes ")) {
            throwValidationError(column, lineIndex, likes);
        }
        return likes;
    }

    private String validateTransport(final int lineIndex, final String[] split) {
        final var column = ParsedColumn.TRANSPORT;
        final var transport = split[column.ordinal()].trim();
        if (fileProcessingConfiguration.getValidationEnabled() && (!transport.startsWith("Rides ") && !transport.startsWith("Drives "))) {
            throwValidationError(column, lineIndex, transport);
        }
        return transport;
    }

    private Double validateNumberField(final int lineIndex, final String[] split, final ParsedColumn column) {
        final var field = split[column.ordinal()].trim();
        try {
            return Double.parseDouble(field);
        } catch (final Exception e) {
            if (fileProcessingConfiguration.getValidationEnabled()) {
                throwValidationError(column, lineIndex, field);
            }
        }
        return null;
    }

    private void validateSpeeds(final Double avgSpeed, final Double topSpeed, final int line) {
        if (fileProcessingConfiguration.getValidationEnabled() && avgSpeed != null && topSpeed != null && topSpeed < avgSpeed) {
            throw new ValidationError("Invalid Top and Avg Speed at line " + line + " , Top speed should be greater than Avg Speed");
        }
    }

    private void throwValidationError(final ParsedColumn column, final int lineIndex, final String fieldValue) {
        throw new ValidationError("Invalid " + column + " at line " + lineIndex + ": " + fieldValue + ".");
    }
}
