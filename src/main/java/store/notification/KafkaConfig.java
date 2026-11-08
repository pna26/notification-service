package store.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import tools.jackson.core.JacksonException;

@Configuration
public class KafkaConfig {

    /** Usado pela fábrica de listeners do Spring Boot (um único bean CommonErrorHandler). */
    @Bean
    DefaultErrorHandler errorHandler(KafkaTemplate<String, String> template) {
        // depois das tentativas, publica em <tópico>-dlt, mesma partição, com a exceção nos headers
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(500L);  // 0,5 s, 1 s, 2 s
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(5_000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        // falhas permanentes: tentar de novo não adianta, vai direto para a DLT
        handler.addNotRetryableExceptions(JacksonException.class, IllegalArgumentException.class);
        return handler;
    }
}
