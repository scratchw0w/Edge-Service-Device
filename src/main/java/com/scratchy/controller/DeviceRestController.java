package com.scratchy.controller;

import com.scratchy.model.Device;
import com.scratchy.model.DeviceFileDto;
import com.scratchy.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/rest")
public class DeviceRestController {

    private final DeviceService service;

    @Operation(summary = "Getting existing device by id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Device was found",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Device.class))}),
            @ApiResponse(responseCode = "404", description = "Device was not found",
                    content = {@Content(mediaType = "application/json")})})
    @GetMapping("/devices/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable("id") String id) {
        log.info("Getting device by id: " + id);
        return service.getDeviceBySerialNumber(id);
    }

    @Operation(summary = "Getting device list by model in json format")
    @ApiResponse(responseCode = "200", description = "Device list was found",
            content = {@Content(mediaType = "application/json")})
    @GetMapping("/devices/json/{model}")
    public ResponseEntity<Iterable<Device>> getDeviceListByModelInJson(
            @PathVariable("model") String model) {
        log.info("Getting devices by model in json format");
        return service.getDeviceListByModelInJson(model);
    }

    @Operation(summary = "Getting device list by model in csv format")
    @ApiResponse(responseCode = "200", description = "Device list was found",
                content = {@Content(mediaType = "text/csv")})
    @GetMapping(value = "/devices/csv/{model}", produces = "text/csv")
    public ResponseEntity<String> getDeviceListByModelInCsv(
            @PathVariable("model") String model) {
        log.info("Getting devices by model in csv format");
        return ResponseEntity.ok(service.getDeviceListByModelInCsv(model));
    }

    @Operation(summary = "Creating new devices from a csv file")
    @ApiResponse(responseCode = "204", description = "Devices were successfully created",
            content = {@Content(mediaType = "application/json")})
    @PostMapping(value = "/devices")
    public ResponseEntity<Iterable<Device>> creatingDevicesFromCsvFile(
            @RequestParam("file") MultipartFile file, Principal principal) {
        log.info("Creating devices from csv file - " + file.getOriginalFilename());
        Iterable<Device> deviceList = service.createDevicesFromCsvFile(file, principal);
        log.info("Sending success email");
        service.sendEmail();
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceList);
    }

    @Operation(summary = "Getting all csvs that were uploaded")
    @ApiResponse(responseCode = "200", description = "Uploaded files list was successfully returned",
            content = {@Content(mediaType = "application/json")})
    @GetMapping("/devices/csv/uploaded")
    public ResponseEntity<Iterable<DeviceFileDto>> getListOfUploadedFiles() {
        log.info("Getting list of uploaded files");
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.getListOfUploadedFiles());
    }
}
