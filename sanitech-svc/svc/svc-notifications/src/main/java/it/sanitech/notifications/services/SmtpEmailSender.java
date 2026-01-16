package it.sanitech.notifications.services;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sender SMTP con retry/backoff (Resilience4j).
 *
 * <p>
 * È separato dal dispatcher per evitare l'auto-invocazione e permettere ad AOP
 * di applicare correttamente {@link Retry}.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class SmtpEmailSender {

    private final JavaMailSender mailSender;

    /**
     * Invia una mail tramite SMTP.
     *
     * <p>
     * Annotato con {@link Retry}: se la chiamata fallisce, Resilience4j ritenta l'invio secondo la policy
     * configurata in {@code resilience4j.retry.instances.notificationEmail}.
     * </p>
     */
    @Retry(name = "notificationEmail")
    public void send(String from, String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }
}
