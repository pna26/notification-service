package store.notification;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {

    private final JavaMailSender mail;
    private final NotificationProperties props;

    public EmailSender(JavaMailSender mail, NotificationProperties props) {
        this.mail = mail;
        this.props = props;
    }

    public void welcome(AccountEvent e) {
        simulateProviderLatency();
        SimpleMailMessage m = new SimpleMailMessage();
        m.setFrom(props.from());
        m.setTo(e.email());
        m.setSubject("Bem-vindo(a) à loja, " + e.name() + "!");
        m.setText("Olá, " + e.name() + ". Sua conta foi criada com sucesso.");
        mail.send(m);
    }

    /** Uma API de e-mail real leva dezenas a centenas de ms por chamada; o Mailpit leva ~1 ms. */
    private void simulateProviderLatency() {
        try {
            Thread.sleep(props.emailLatency());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }
}
