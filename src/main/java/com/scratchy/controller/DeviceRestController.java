package com.scratchy.controller;

import com.scratchy.model.Device;
import com.scratchy.model.DeviceFileDto;
import com.scratchy.service.DeviceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/rest")
public class DeviceRestController {

    private final DeviceService service;

    @GetMapping("/devices/{id}")
    public Flux<Device> getDeviceById(@PathVariable("id") String id) {
        log.info("Getting device by id: " + id);
        return service.getDeviceBySerialNumber(id);
    }

    @GetMapping("/devices/json/{model}")
    public ResponseEntity<Iterable<Device>> getDeviceListByModelInJson(
            @PathVariable("model") String model) {
        log.info("Getting devices by model in json format");
        return service.getDeviceListByModelInJson(model);
    }

    @GetMapping(value = "/devices/csv/{model}", produces = "text/csv")
    public ResponseEntity<String> getDeviceListByModelInCsv(
            @PathVariable("model") String model) {
        log.info("Getting devices by model in csv format");
        return ResponseEntity.ok(service.getDeviceListByModelInCsv(model));
    }

    @PostMapping(value = "/devices")
    public ResponseEntity<Iterable<Device>> creatingDevicesFromCsvFile(
            @RequestParam("file") MultipartFile file) {
        log.info("Creating devices from csv file - " + file.getOriginalFilename());
        Iterable<Device> deviceList = service.createDevicesFromCsvFile(file);
        log.info("Sending success email");
        service.sendEmail();
        return ResponseEntity.ok(deviceList);
    }

    @GetMapping("/devices/csv/uploaded")
    public ResponseEntity<Iterable<DeviceFileDto>> getListOfUploadedFiles() {
        log.info("Getting list of uploaded files");
        return ResponseEntity.status(HttpStatus.OK).body(service.getListOfUploadedFiles());
    }
}
