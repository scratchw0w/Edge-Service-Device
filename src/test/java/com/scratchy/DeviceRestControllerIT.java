package com.scratchy;

import com.scratchy.model.Device;
import com.scratchy.model.DeviceFileDto;
import com.scratchy.repository.DeviceFileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = EdgeApplication.class)
@ActiveProfiles("dev")
public class DeviceRestControllerIT {

    private static final String BASE_URL = "http://localhost:";
    private static final int PORT = 8081;
    private static final int SERVICE_PORT = 8080;

    private final Device testDevice = new Device("test", "test", "test");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DeviceFileRepository repository;

    @BeforeEach
    public void insertNewDevice() {
        this.restTemplate
                .postForEntity(BASE_URL + SERVICE_PORT + "/api/devices",
                    getRequestForDevice(this.testDevice), Device.class);
    }

    @AfterEach
    public void deleteExistingDevice() {
        this.restTemplate
                .exchange(BASE_URL + SERVICE_PORT + "/api/devices/" + this.testDevice.getId(),
                        HttpMethod.DELETE, getRequestForDevice(this.testDevice), Device.class);
    }

    @Test
    public void shouldReturnOkStatusForGettingExistingDeviceById() {
        ResponseEntity<Device> responseEntity = this.restTemplate
                .getForEntity(BASE_URL + PORT + "/rest/devices/" + testDevice.getId(),
                        Device.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(testDevice, responseEntity.getBody());
    }

    @Test
    public void shouldReturn404ForGettingNonExistingUser() {
        ResponseEntity<Device> responseEntity = this.restTemplate
                .getForEntity(BASE_URL + PORT + "/rest/devices/invalid_id/",
                        Device.class);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturnOkStatusForGettingDevicesByModelInJson() {
        ResponseEntity<Iterable<Device>> responseEntity = this.restTemplate
                .exchange(BASE_URL + PORT + "/rest/devices/json/" + testDevice.getModel(), HttpMethod.GET,
                        null, new ParameterizedTypeReference<Iterable<Device>>() {});
        Iterable<Device> devices = List.of(this.testDevice);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(devices, responseEntity.getBody());
    }

    @Test
    public void shouldReturnOkStatusForGettingDevicesByModelInCsv() {
        ResponseEntity<String> responseEntity = this.restTemplate
                .exchange(BASE_URL + PORT + "/rest/devices/csv/" + testDevice.getModel(), HttpMethod.GET,
                        null, String.class);

        String expectedResponse = ("id," + "model," + "description" + "\n" +
                this.testDevice.getId() + "," +
                this.testDevice.getModel() + "," +
                this.testDevice.getDescription())
                .replaceAll("\\n|\\r\\n", System.getProperty("line.separator"));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse.trim(), responseEntity.getBody().trim());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/test.csv", numLinesToSkip = 1)
    public void shouldCreateNewDeviceAndReturnCreatedStatusForCreatingNewDevice(
            String id, String model, String description) {
    }

    @Test
    public void shouldReturnOkAndOneRecord() {
        DeviceFileDto fileDto = new DeviceFileDto("file.csv", 1, "user");
        fileDto.setDateOfUploading(LocalDateTime.of(1, 1, 1, 1, 1));
        repository.save(fileDto);
        ResponseEntity<Iterable<DeviceFileDto>> responseEntity = this.restTemplate
                .exchange(BASE_URL + PORT + "/rest/devices/csv/uploaded", HttpMethod.GET,
                        null, new ParameterizedTypeReference<Iterable<DeviceFileDto>>() {});

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(List.of(fileDto), responseEntity.getBody());

        repository.delete(fileDto);
    }

    private HttpEntity<Device> getRequestForDevice(Device device) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(device, headers);
    }
}
