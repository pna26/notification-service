package store.notification;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "store.notification")
public record NotificationProperties(
        @DefaultValue("account-events") String topic,
        @DefaultValue("50ms") Duration emailLatency,
        @DefaultValue("true") boolean idempotent,
        @DefaultValue("boas-vindas@store.local") String from) {
}
