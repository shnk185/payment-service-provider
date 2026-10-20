package org.ngs.paymentserviceprovider.config;

import org.ngs.paymentserviceprovider.config.properties.WebhookConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class WebhookHttpClientConfig {

    @Bean
    public HttpClient webhookHttpClient(WebhookConfig webhookConfig) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(webhookConfig.getConnectTimeoutSeconds()))
                .build();
    }
}
