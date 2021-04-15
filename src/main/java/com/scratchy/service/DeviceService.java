package com.scratchy.service;

import com.scratchy.model.Device;
import com.scratchy.model.DeviceFileDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

public interface DeviceService {

    ResponseEntity<Device> getDeviceBySerialNumber(String serialNumber);

    ResponseEntity<Iterable<Device>> getDeviceListByModelInJson(String model);

    String getDeviceListByModelInCsv(String model);

    Iterable<Device> createDevicesFromCsvFile(MultipartFile file, Principal principal);

    void sendEmail();

    Iterable<DeviceFileDto> getListOfUploadedFiles();
}
