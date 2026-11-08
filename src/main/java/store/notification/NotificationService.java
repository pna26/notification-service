package store.notification;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JdbcClient jdbc;
    private final EmailSender email;
    private final NotificationProperties props;

    public NotificationService(JdbcClient jdbc, EmailSender email, NotificationProperties props) {
        this.jdbc = jdbc;
        this.email = email;
        this.props = props;
    }

    /**
     * Entrega at-least-once: o mesmo evento pode chegar duas vezes. O eventId é
     * gravado na mesma transação do registro do e-mail; se já existir, é duplicata.
     *
     * @return true se enviou o e-mail, false se o evento era duplicado
     */
    @Transactional
    public boolean handle(AccountEvent e) {
        if (props.idempotent()) {
            int fresh = jdbc.sql("INSERT INTO processed_events (event_id) VALUES (?) ON CONFLICT DO NOTHING")
                    .param(e.eventId())
                    .update();
            if (fresh == 0) {
                log.info("duplicate event {} for account {} ignored", e.eventId(), e.accountId());
                return false;
            }
        }
        email.welcome(e); // se lançar, a transação inteira volta (inclusive a linha de deduplicação)
        jdbc.sql("""
                INSERT INTO notifications (event_id, account_id, email, kind, occurred_at)
                VALUES (?, ?, ?, ?, ?)
                """)
                .params(e.eventId(), e.accountId(), e.email(), e.eventType(),
                        OffsetDateTime.ofInstant(e.occurredAt(), ZoneOffset.UTC))
                .update();
        log.info("welcome e-mail sent for account {}", e.accountId());
        return true;
    }
}
