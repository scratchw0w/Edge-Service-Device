package com.scratchy.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class DeviceFileDto {

    @Id
    private String fileName;

    private int deviceCount;

    @Column(updatable = false)
    private LocalDateTime dateOfUploading;

    @Column(updatable = false)
    private String userName;

    public DeviceFileDto(String fileName, int deviceCount, String userName) {
        this.fileName = fileName;
        this.deviceCount = deviceCount;
        this.dateOfUploading = LocalDateTime.now();
        this.userName = userName;
    }
}
