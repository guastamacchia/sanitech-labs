package it.sanitech.notifications.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Abilita il binding delle {@link NotificationsProperties}.
 */
@Configuration
@EnableConfigurationProperties(NotificationsProperties.class)
public class NotificationsConfig {
}
