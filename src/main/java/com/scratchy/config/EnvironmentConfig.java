package com.scratchy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.env")
public class EnvironmentConfig {
    private String url;
    private String port;
}
