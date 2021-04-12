package com.scratchy.repository;

import com.scratchy.model.DeviceFileDto;
import org.springframework.data.repository.CrudRepository;

public interface DeviceFileRepository extends CrudRepository<DeviceFileDto, String> {}
