package it.sanitech.directory.integrations.captcha;

/**
 * Eccezione lanciata quando la verifica CAPTCHA fallisce.
 */
public class CaptchaVerificationException extends RuntimeException {

    public CaptchaVerificationException(String message) {
        super(message);
    }

    public CaptchaVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
