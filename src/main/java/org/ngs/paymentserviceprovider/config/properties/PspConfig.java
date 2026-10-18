package org.ngs.paymentserviceprovider.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "psp")
public class PspConfig {
    private String baseUrl;
    private long sessionTtlMinutes;
}
