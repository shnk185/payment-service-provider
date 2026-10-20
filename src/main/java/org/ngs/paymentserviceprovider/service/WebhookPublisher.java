package org.ngs.paymentserviceprovider.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.paymentserviceprovider.config.properties.WebhookConfig;
import org.ngs.paymentserviceprovider.entity.WebhookDeliveryEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookPublisher {
    private final WebhookService webhookService;
    private final WebhookConfig webhookConfig;
    private final HttpClient webhookHttpClient;

    @Scheduled(fixedDelayString = "${psp.webhook.poll-interval-ms:2000}")
    public void deliverPending() {
        if (!webhookConfig.isEnabled()) {
            return;
        }
        List<WebhookDeliveryEntity> deliveries = webhookService.claimBatch();
        deliveries.forEach(this::deliver);
    }

    @Scheduled(cron = "${psp.webhook.purge-cron:0 0 3 * * *}")
    public void purgeDelivered() {
        webhookService.purge();
    }

    private void deliver(WebhookDeliveryEntity delivery) {
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            HttpRequest request = HttpRequest.newBuilder(URI.create(delivery.getTargetUrl()))
                    .timeout(Duration.ofSeconds(webhookConfig.getReadTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Psp-Signature", signature(timestamp, delivery.getPayload()))
                    .POST(HttpRequest.BodyPublishers.ofString(delivery.getPayload(), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = webhookHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 == 2) {
                webhookService.markDelivered(delivery);
            } else {
                webhookService.markFailed(delivery, "HTTP " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("webhook {} to {} failed", delivery.getEventId(), delivery.getTargetUrl(), e);
            webhookService.markFailed(delivery, e.getMessage());
        }
    }

    private String signature(long timestamp, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookConfig.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));
            return "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("could not sign webhook payload", e);
        }
    }
}
