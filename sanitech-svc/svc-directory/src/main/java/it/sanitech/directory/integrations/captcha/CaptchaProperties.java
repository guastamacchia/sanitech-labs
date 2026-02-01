package it.sanitech.directory.integrations.captcha;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

/**
 * Proprietà di configurazione per reCAPTCHA v3.
 */
@Validated
@ConfigurationProperties(prefix = "sanitech.captcha")
public record CaptchaProperties(
        boolean enabled,
        @Nullable String secretKey,
        String verifyUrl,
        double scoreThreshold
) {}
