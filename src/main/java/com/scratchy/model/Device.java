package com.scratchy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9-\\s]*$", message = "Serial Number is incorrect")
    private String id;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Model is incorrect")
    private String model;

    private String description;
}
