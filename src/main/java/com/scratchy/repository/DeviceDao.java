package com.scratchy.repository;

import com.scratchy.model.Device;

import java.util.Optional;

public interface DeviceDao {

    Optional<Iterable<Device>> getDeviceList();

    Optional<Device> getDeviceById(String id);

    void saveDevice(Device device);

    Optional<Device> updateDevice(String id, Device device);

    Optional<Device> deleteDeviceById(String id);
}
