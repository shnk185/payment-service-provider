package org.ngs.paymentserviceprovider.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "psp.webhook")
public class WebhookConfig {
    private boolean enabled = false;
    private String url;
    private String secret;
    private int batchSize = 20;
    private long pollIntervalMs = 2000;
    private int connectTimeoutSeconds = 5;
    private int readTimeoutSeconds = 10;
    private int leaseSeconds = 120;
    private int maxAttempts = 8;
    private long baseBackoffSeconds = 30;
    private long maxBackoffSeconds = 3600;
    private int purgeAfterDays = 7;
}
