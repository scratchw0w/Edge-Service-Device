package com.scratchy.service;

import com.scratchy.model.Device;
import com.scratchy.model.DeviceFileDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

public interface DeviceService {

    Flux<Device> getDeviceBySerialNumber(String serialNumber);

    ResponseEntity<Iterable<Device>> getDeviceListByModelInJson(String model);

    String getDeviceListByModelInCsv(String model);

    Iterable<Device> createDevicesFromCsvFile(MultipartFile file);

    void sendEmail();

    Iterable<DeviceFileDto> getListOfUploadedFiles();
}
