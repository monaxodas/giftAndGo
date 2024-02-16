package com.giftandgo.assessment.assessment.web;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giftandgo.assessment.jpa.RequestLogEntityRepository;
import com.giftandgo.assessment.model.GeolocationInfo;
import com.giftandgo.assessment.model.OutputFileEntry;
import com.giftandgo.assessment.model.ParsedFile;

import lombok.SneakyThrows;

@SpringBootTest(properties = {"geolocation.client.url=https://7yzz3.wiremockapi.cloud/json/{query}",
"file.configuration.ip-data-centers-block-list=Azure",
"file.configuration.ip-countries-block-list=USA"})
@AutoConfigureMockMvc
class FileControllerIntegrationTest {

    private static String validIp = "1";
    private static String blockedCountryIp = "2";
    private static String blockedIspIp = "3";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestLogEntityRepository logEntityRepository;

    @BeforeEach
    void setup() {
        logEntityRepository.deleteAll();
    }

    @SneakyThrows
    @Test
    void testHappyPath() {
        var file = getMultipartFile("InputFile.txt");

        final var response = mockMvc.perform(multipart("/file/process")
                .file("file", file.getBytes())
                .remoteAddress(validIp)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        final var parsedFile = response.getResponse().getContentAsString();
        final var isp = "Le Groupe Videotron Ltee";
        final var expected = expectedFile("Canada", isp);

        assertThat(objectMapper.writeValueAsString(expected)).isEqualTo(parsedFile);

        assertLogEntry(HttpStatus.OK, isp, validIp);

    }

    @SneakyThrows
    @Test
    void testBlockedCountry() {
        var file = getMultipartFile("InputFile.txt");
        final var response = mockMvc.perform(multipart("/file/process")
                .file("file", file.getBytes())
                .remoteAddress(blockedCountryIp)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isForbidden())
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).isEqualTo("Country USA is not allowed to invoke this API");

        assertLogEntry(HttpStatus.FORBIDDEN, "Le Groupe Videotron Ltee", blockedCountryIp);
    }

    @SneakyThrows
    @Test
    void testBlockedIsp() {
        var file = getMultipartFile("InputFile.txt");
        final var response = mockMvc.perform(multipart("/file/process")
                .file("file", file.getBytes())
                .remoteAddress(blockedIspIp)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isForbidden())
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).isEqualTo("Requests from Isp/Data center: Azure are not allowed to invoke this API");

        assertLogEntry(HttpStatus.FORBIDDEN, "Le Groupe Videotron Ltee", blockedIspIp);
    }

    @SneakyThrows
    @Test
    void testInvalidFile() {
        var file = getMultipartFile("InvalidFile.txt");
        final var response = mockMvc.perform(multipart("/file/process")
                .file("file", file.getBytes())
                .remoteAddress(validIp)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).isEqualTo("Invalid number of columns for line: 0");

        assertLogEntry(HttpStatus.BAD_REQUEST, "Azure", validIp);
    }


    @SneakyThrows
    private MockMultipartFile getMultipartFile(final String filename) {
        final ClassPathResource resource = new ClassPathResource(filename);
        return new MockMultipartFile(filename, resource.getInputStream());
    }

    private ParsedFile expectedFile(final String country, final String isp) {
        return new ParsedFile(List.of(
            new OutputFileEntry("John Smith", "Rides A Bike", 12.1),
            new OutputFileEntry("Mike Smith", "Drives an SUV", 95.5),
            new OutputFileEntry("Jenny Walters", "Rides A Scooter", 15.3)
        ), getGeolocationInfo(country, isp));
    }

    private GeolocationInfo getGeolocationInfo(final String country, final String isp) {
        return new GeolocationInfo("24.48.0.1", "success", country, "CA", "QC",
            "Quebec", "Montreal", "H1K", "45.6085", "-73.5493", "America/Toronto",
            isp, "Videotron Ltee", "AS5769 Videotron Ltee");
    }

    private void assertLogEntry(final HttpStatus expectedStatus, final String isp, final String ipAddress) {
        final var entities = logEntityRepository.findAll();
        assertThat(entities.size()).isEqualTo(1);
        final var requestLogEntity = entities.get(0);
        assertThat(requestLogEntity.getId()).isNotNull();
        assertThat(requestLogEntity.getUri()).isEqualTo("/file/process");
        assertThat(requestLogEntity.getTimestamp()).isNotNull();
        assertThat(requestLogEntity.getResponseCode()).isEqualTo(expectedStatus);
        assertThat(requestLogEntity.getIpAddress()).isEqualTo(ipAddress);
        Optional.ofNullable(requestLogEntity.getCountryCode()).ifPresent(c -> assertThat(c).isEqualTo("CA"));;
        Optional.ofNullable(requestLogEntity.getIsp()).ifPresent(c -> assertThat(c).isEqualTo(isp));;
        assertThat( requestLogEntity.getTimeElapsed()).isNotZero();
        assertThat(requestLogEntity.getCreatedDate()).isNotNull();


    }

}
