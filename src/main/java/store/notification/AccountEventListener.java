package store.notification;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
public class AccountEventListener {

    private final ObjectMapper mapper;
    private final NotificationService notifications;

    public AccountEventListener(ObjectMapper mapper, NotificationService notifications) {
        this.mapper = mapper;
        this.notifications = notifications;
    }

    @KafkaListener(topics = "${store.notification.topic}", groupId = "notification")
    public void onAccountEvent(ConsumerRecord<String, String> record) {
        // JSON inválido lança JacksonException: o KafkaConfig manda direto para a DLT
        AccountEvent event = mapper.readValue(record.value(), AccountEvent.class);
        if (event.eventId() == null || event.eventType() == null || event.email() == null) {
            throw new IllegalArgumentException("não é um evento da loja: offset " + record.offset());
        }
        if (!"AccountCreated".equals(event.eventType())) {
            return; // outros tipos de evento não são para este consumidor (ainda)
        }
        notifications.handle(event);
    }
}
