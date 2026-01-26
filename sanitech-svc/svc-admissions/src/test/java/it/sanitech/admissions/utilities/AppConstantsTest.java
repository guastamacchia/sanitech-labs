package it.sanitech.admissions.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AppConstantsTest {

    @Test
    void exposesProblemAndErrorConstants() {
        assertThat(AppConstants.Problem.TYPE_NO_BEDS)
                .isEqualTo("https://sanitech.it/problems/no-beds-available");
        assertThat(AppConstants.ErrorMessage.ERR_NO_BEDS)
                .isEqualTo("Posti letto non disponibili");
    }

    @Test
    void exposesSortableAdmissionsFields() {
        assertThat(AppConstants.SortFields.ADMISSIONS)
                .containsExactlyInAnyOrder(
                        "id",
                        "patientId",
                        "departmentCode",
                        "admissionType",
                        "status",
                        "admittedAt",
                        "dischargedAt"
                );
    }

    @Test
    void exposesSpringDefaults() {
        assertThat(AppConstants.Spring.KEY_APP_NAME).isEqualTo("spring.application.name");
        assertThat(AppConstants.Spring.DEFAULT_APP_NAME).isEqualTo("svc-admissions");
        assertThat(AppConstants.Spring.KEY_SERVER_PORT).isEqualTo("server.port");
        assertThat(AppConstants.Spring.DEFAULT_SERVER_PORT).isEqualTo("8084");
    }
}
