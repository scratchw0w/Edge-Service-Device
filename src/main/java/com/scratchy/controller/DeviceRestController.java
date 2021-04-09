package com.scratchy.controller;

import com.scratchy.config.EnvironmentConfig;
import com.scratchy.model.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rest")
public class DeviceRestController {

    private final WebClient webClient;
    private EnvironmentConfig environmentConfig;

    public DeviceRestController(EnvironmentConfig config) {
        this.environmentConfig = config;
        this.webClient = WebClient.builder().baseUrl(environmentConfig.getUrl() +
                environmentConfig.getPort()).build();
    }

    @GetMapping("/devices")
    public Mono<ResponseEntity<List<Device>>> getDeviceList() {
        log.info("Getting device list");
        return webClient.get()
                .uri("/api/devices")
                .retrieve()
                .toEntityList(Device.class);
    }

    @GetMapping("/devices/{id}")
    public Flux<Device> getDeviceById(@PathVariable("id") String id) {
        log.info("Getting device by id: " + id);
        return webClient.get()
                .uri("/api/devices/" + id)
                .retrieve()
                .bodyToFlux(Device.class);
    }

    @PostMapping("/devices")
    public Mono<Device> createDevice(@Valid @RequestBody Device device) {
        log.info("Creating new device with id: " + device.getId());
        return webClient.post()
                .uri("/api/devices")
                .body(Mono.just(device), Device.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Device.class);
    }

    @PutMapping("/devices/{id}")
    public Mono<Device> updateDevice(@Valid @RequestBody Device device,
                                     @PathVariable("id") String id) {
        log.info("Updating device with id: " + id);
        return webClient.put()
                .uri("/api/devices/" + id)
                .body(Mono.just(device), Device.class)
                .retrieve()
                .bodyToMono(Device.class);
    }

    @DeleteMapping("/devices/{id}")
    public Flux<Device> deleteDevice(@PathVariable("id") String id) {
        log.info("Deleting device with id: " + id);
        return webClient.delete()
                .uri("/api/devices/" + id)
                .headers((header) -> header.setContentType(MediaType.APPLICATION_JSON))
                .retrieve()
                .bodyToFlux(Device.class);
    }
}
