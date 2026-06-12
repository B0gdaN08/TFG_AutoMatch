package es.urjc.automatch.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptServiceTest {

    @Test
    void blocksAfterTooManyFailedAttempts() {
        LoginAttemptService service = new LoginAttemptService();
        String email = "user@automatch.es";
        String ip = "127.0.0.1";

        for (int i = 0; i < LoginAttemptService.MAX_ATTEMPTS; i++) {
            assertThatCode(() -> service.checkAllowed(email, ip)).doesNotThrowAnyException();
            service.onFailedAttempt(email, ip);
        }

        assertThatThrownBy(() -> service.checkAllowed(email, ip))
                .isInstanceOf(LoginAttemptService.TooManyAttemptsException.class);
    }

    @Test
    void successfulLoginResetsCounter() {
        LoginAttemptService service = new LoginAttemptService();
        String email = "user@automatch.es";
        String ip = "127.0.0.1";

        for (int i = 0; i < LoginAttemptService.MAX_ATTEMPTS; i++) {
            service.onFailedAttempt(email, ip);
        }
        service.onSuccessfulLogin(email, ip);

        assertThatCode(() -> service.checkAllowed(email, ip)).doesNotThrowAnyException();
    }

    @Test
    void differentIpsAreTrackedIndependently() {
        LoginAttemptService service = new LoginAttemptService();
        String email = "user@automatch.es";

        for (int i = 0; i < LoginAttemptService.MAX_ATTEMPTS; i++) {
            service.onFailedAttempt(email, "1.1.1.1");
        }

        assertThatThrownBy(() -> service.checkAllowed(email, "1.1.1.1"))
                .isInstanceOf(LoginAttemptService.TooManyAttemptsException.class);
        assertThatCode(() -> service.checkAllowed(email, "2.2.2.2")).doesNotThrowAnyException();
    }
}
