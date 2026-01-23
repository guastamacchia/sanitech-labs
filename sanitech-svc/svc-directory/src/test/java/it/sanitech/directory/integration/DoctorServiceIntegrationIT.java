package it.sanitech.directory.integration;

import it.sanitech.directory.services.DoctorService;
import it.sanitech.directory.services.dto.DoctorDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@TestPropertySource(properties = {
        "OAUTH2_SCHEME=http",
        "OAUTH2_HOST=test-auth",
        "OAUTH2_PORT=8081",
        "OAUTH2_REALM=sanitech",
        "KAFKA_HOST=localhost",
        "KAFKA_PORT=9092",
        "CORS_ALLOWED_ORIGINS=*"
})
class DoctorServiceIntegrationIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("directory")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
        registry.add("DATABASE_HOST", POSTGRES::getHost);
        registry.add("DATABASE_PORT", () -> POSTGRES.getMappedPort(5432));
        registry.add("DATABASE_NAME", POSTGRES::getDatabaseName);
        registry.add("DATABASE_USER", POSTGRES::getUsername);
        registry.add("DATABASE_PASSWORD", POSTGRES::getPassword);
    }

    @Autowired
    private DoctorService doctorService;

    @Test
    void shouldLoadSeededDoctorsThroughService() {
        var page = doctorService.search(null, null, null, 0, 10, null);

        assertThat(page.getContent()).isNotEmpty();
        DoctorDto first = page.getContent().get(0);
        assertThat(first.email()).isNotBlank();
        assertThat(first.departments()).isNotEmpty();
        assertThat(first.specializations()).isNotEmpty();
    }
}
