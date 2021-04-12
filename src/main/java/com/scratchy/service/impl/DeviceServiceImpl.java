package com.scratchy.service.impl;

import com.scratchy.config.EnvironmentConfig;
import com.scratchy.model.Device;
import com.scratchy.model.DeviceFileDto;
import com.scratchy.repository.DeviceFileRepository;
import com.scratchy.service.DeviceService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final WebClient webClient;
    private DeviceFileRepository repository;

    public DeviceServiceImpl(EnvironmentConfig config) {
        this.webClient = WebClient.builder()
                .baseUrl(config.getUrl() + config.getPort())
                .build();
    }

    @Autowired
    public void setRepository(DeviceFileRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<Device> getDeviceBySerialNumber(String serialNumber) {
        return webClient.get()
                .uri("/api/devices/" + serialNumber)
                .retrieve()
                .bodyToFlux(Device.class);
    }

    @Override
    public ResponseEntity<Iterable<Device>> getDeviceListByModelInJson(String model) {
        return ResponseEntity.ok().body(getDeviceListByModel(model));
    }

    @Override
    public void getDeviceListByModelInCsv(String model) {

    }

    private List<Device> getDeviceListByModel(String model) {
        List<Device> deviceList = Objects.requireNonNull(webClient
                .get()
                .uri("/api/devices")
                .retrieve()
                .toEntityList(Device.class)
                .block())
                .getBody();

        return Objects.requireNonNull(deviceList)
                .stream()
                .filter(value -> value.getModel().equals(model))
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Device> createDevicesFromCsvFile(MultipartFile file) {
        List<Device> devices;
        try {
            devices = parseCsvFile(file);
        } catch (IOException exception) {
            throw new RuntimeException("During parsing an csv file an exception occurred",
                    exception);
        }

        postDeviceListToUri(devices);
        DeviceFileDto fileDto = new DeviceFileDto(file.getOriginalFilename(),
                devices.size());
        repository.save(fileDto);
        return devices;
    }

    private List<Device> parseCsvFile(MultipartFile file) throws IOException {
        List<Device> deviceList = new ArrayList<>();
        CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader()
                .parse(new InputStreamReader(new ByteArrayInputStream(file.getBytes())));
        for (CSVRecord record : csvParser) {
            String id = record.get("id");
            String model = record.get("model");
            String description = record.get("description");
            deviceList.add(new Device(id, model, description));
        }

        return deviceList;
    }

    private void postDeviceListToUri(List<Device> deviceList) {
        for (Device device: deviceList) {
            webClient
                    .post()
                    .uri("/api/devices")
                    .body(Mono.just(device), Device.class)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Device.class)
                    .block();
        }
    }

    @Override
    public void sendEmail(String email) {

    }

    @Override
    public Iterable<DeviceFileDto> getListOfUploadedFiles() {
        return repository.findAll();
    }
}
