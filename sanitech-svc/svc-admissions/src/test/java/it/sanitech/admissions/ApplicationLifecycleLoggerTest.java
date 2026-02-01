package it.sanitech.admissions;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.sanitech.admissions.utilities.AppConstants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

class ApplicationLifecycleLoggerTest {

    @Test
    void onReadyReadsApplicationNameAndPort() {
        Environment environment = Mockito.mock(Environment.class);
        when(environment.getProperty(AppConstants.Spring.KEY_APP_NAME, AppConstants.Spring.DEFAULT_APP_NAME))
                .thenReturn("svc-admissions");
        when(environment.getProperty(AppConstants.Spring.KEY_SERVER_PORT, AppConstants.Spring.DEFAULT_SERVER_PORT))
                .thenReturn("8084");

        ApplicationLifecycleLogger logger = new ApplicationLifecycleLogger(environment);
        logger.onReady();

        verify(environment).getProperty(AppConstants.Spring.KEY_APP_NAME, AppConstants.Spring.DEFAULT_APP_NAME);
        verify(environment).getProperty(AppConstants.Spring.KEY_SERVER_PORT, AppConstants.Spring.DEFAULT_SERVER_PORT);
    }

    @Test
    void onStoppingReadsApplicationName() {
        Environment environment = Mockito.mock(Environment.class);
        when(environment.getProperty(AppConstants.Spring.KEY_APP_NAME, AppConstants.Spring.DEFAULT_APP_NAME))
                .thenReturn("svc-admissions");

        ApplicationLifecycleLogger logger = new ApplicationLifecycleLogger(environment);
        logger.onStopping();

        verify(environment).getProperty(AppConstants.Spring.KEY_APP_NAME, AppConstants.Spring.DEFAULT_APP_NAME);
    }
}
